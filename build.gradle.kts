import deps.DependencyConfig
import deps.Loaders

plugins {
    id("dev.isxander.modstitch.base") version "clefal-version"
    id("dev.isxander.modstitch.publishing") version "clefal-version"
    id ("org.jetbrains.kotlin.jvm") version "2.1.10"
    id ("org.jetbrains.kotlin.plugin.serialization") version "2.1.10"
}

fun prop(name: String, consumer: (prop: String) -> Unit) {
    (findProperty(name) as? String?)?.let(consumer)
}


val modv = property("mod_version") as String


val loader = when {
    modstitch.isLoom -> "fabric"
    modstitch.isModDevGradleRegular -> "neoforge"
    modstitch.isModDevGradleLegacy -> "forge"
    else -> throw IllegalStateException("Unsupported loader")
}

val minecraft = property("deps.minecraft") as String

modstitch {
    minecraftVersion = minecraft

    // Alternatively use stonecutter.eval if you have a lot of versions to target.
    // https://stonecutter.kikugie.dev/stonecutter/guide/setup#checking-versions
    javaVersion = when (minecraft) {
        "1.20.1" -> 17
        "1.21.1" -> 21
        "1.21.4" -> 21
        "1.21.8", "1.21.10", "1.21.11" -> 21
        "26.2", "26.3" -> 25
        else -> throw IllegalArgumentException("Please store the java version for $minecraft in build.gradle.kts!")
    }

    // If parchment doesnt exist for a version yet you can safely
    // omit the "deps.parchment" property from your versioned gradle.properties
    parchment {
        prop("deps.parchment") {
            if (minecraft == "1.21.1") minecraftVersion.set("1.21")
            mappingsVersion = it
        }
    }

    // This metadata is used to fill out the information inside
    // the metadata files found in the templates folder.
    val mid = "lootbeams"
    metadata {
        modId = mid
        modName = "Loot Beams Refork"
        modVersion = modv
        modGroup = "com.clefal"
        modAuthor = "Clefal"
        modDescription =
            "Loot items, guided by light!"
        modLicense = "MIT"
        fun <K : Any, V : Any> MapProperty<K, V>.populate(block: MapProperty<K, V>.() -> Unit) {
            block()
        }
        replacementProperties.populate {
            // You can put any other replacement properties/metadata here that
            // modstitch doesn't initially support. Some examples below.
            put("mod_issue_tracker", "https://github.com/TUsama/Loot-Beams-Refork/issues")
            val packFormat = when (property("deps.minecraft")) {
                    "1.20.1" -> 15
                    "1.21.1" -> 34
                    "1.21.4" -> 46
                    "1.21.8" -> 64
                    "1.21.10" -> 69
                    "1.21.11" -> 70.0
                    "26.2" -> 84.0
                    "26.3" -> 97
                    else -> throw IllegalArgumentException("Please store the resource pack version for ${property("deps.minecraft")} in build.gradle.kts! https://minecraft.wiki/w/Pack_format")
            }.toString()
            put("pformat", packFormat)

            put("target_minecraft", minecraft)
            // MC 26.3 replaced pack.mcmeta's "pack_format" with "min_format"/"max_format" (the
            // latter carrying the minor version). Leaving the old key makes the loader log
            // "Error reading optional pack metadata ... attempting fallback type" on every launch.
            put(
                "pack_format_entry", when (property("deps.minecraft")) {
                    "26.3" -> "\"min_format\": 97, \"max_format\": [97, 1]"
                    else -> "\"pack_format\": $packFormat"
                }
            )
            //put("target_lib", property("deps.lib") as String)
            put(
                "target_loader", when (loader) {
                    "neoforge" -> property("deps.neoforge") as String
                    else -> ""
                }
            )
            put("loader", loader)
            put(
                "target_fabricloader", when (loader) {
                    "fabric" -> property("deps.fabric_loader") as String
                    else -> ""
                }
            )
            put("fzzy_config_version", property("deps.fzzy_config_version") as String)
            put("lib_version", property("deps.lib_version") as String)
        }
    }

    // Fabric Loom (Fabric)
    loom {
        // It's not recommended to store the Fabric Loader version in properties.
        // Make sure its up to date.
        fabricLoaderVersion = when (minecraft) {
            "26.3" -> "0.19.5"
            "26.2" -> "0.19.3"
            else -> "0.16.11"
        }
        configureLoom {
            runs {
                all {
                    ideConfigGenerated(true)
                }
                //accessWidenerPath.set(file("../../src/main/resources/${mid}.accesswidener"))
            }
        }
    }

    // ModDevGradle (NeoForge, Forge, Forgelike)
    moddevgradle {

        prop("deps.forge") { forgeVersion = it }
        prop("deps.neoform") { neoFormVersion = it }
        prop("deps.neoforge") { neoForgeVersion = it }
        prop("deps.mcp") { mcpVersion = it }


        // Configures client and server runs for MDG, it is not done by default
        defaultRuns()

        // This block configures the `neoforge` extension that MDG exposes by default,
        // you can configure MDG like normal from here
        configureNeoForge {
            //setAccessTransformers("../../src/main/resources/META-INF/accesstransformer.cfg")
            validateAccessTransformers = false
            afterEvaluate {
                runs.all {
                    val upperName = name.replaceFirstChar {
                        it.uppercaseChar()
                    }
                    tasks.named<JavaExec>("run$upperName") {
                        javaLauncher.set(
                            javaToolchains.launcherFor {
                                languageVersion = JavaLanguageVersion.of(project.modstitch.javaVersion.get())
                                vendor = JvmVendorSpec.JETBRAINS
                            }
                        )
                    }
                    jvmArguments.add("-XX:+AllowEnhancedClassRedefinition")
                    disableIdeRun()

            }
                //gameDirectory = file("run")
            }
        }
    }

    mixin {
        // You do not need to specify mixins in any mods.json/toml file if this is set to
        // true, it will automatically be generated.
        addMixinsToModManifest = true
        when {
            isModDevGradleLegacy -> configs.register("${mid}-1.20.1")
            minecraft == "1.21.1" -> configs.register("${mid}-1.21")
            minecraft == "1.21.4" -> configs.register("${mid}-1.21.4")
            minecraft == "26.2" -> configs.register("${mid}-26.2")
            minecraft == "26.3" -> configs.register("${mid}-26.3")
            minecraft == "1.21.10" || minecraft == "1.21.11" -> configs.register("${mid}-1.21.10")
            else -> configs.register("${mid}-default")
        }


        // Most of the time you wont ever need loader specific mixins.
        // If you do, simply make the mixin file and add it like so for the respective loader:
        // if (isLoom) configs.register("examplemod-fabric")
        // if (isModDevGradleRegular) configs.register("examplemod-neoforge")
        // if (isModDevGradleLegacy) configs.register("examplemod-forge")
    }
}
base {
    val meta = modstitch.metadata
    archivesName = "${meta.modName.get()}-${loader}-${minecraft}"
}

// Stonecutter constants for mod loaders.
// See https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-constants
stonecutter {
    constants.putAll(mapOf<String, Boolean>(
        "fabric" to loader.equals("fabric"),
        "neoforge" to loader.equals("neoforge"),
        "forge" to loader.equals("forge"),
        "vanilla" to loader.equals("vanilla"),
        "legacy" to (minecraft == "1.20.1"),
        "malum" to (modstitch.minecraftVersion.get() == "1.20.1" || (loader.equals("neoforge") && modstitch.minecraftVersion.get() == "1.21.1")),
        "biomancy" to (loader.equals("forge") && (minecraft == "1.20.1")),
        "simplesword" to ((minecraft == "1.21.1") || (minecraft == "1.20.1"))
    ))

    replacements.string(current.version >= "1.21.10" && loader.equals("fabric")) {
        replace("guiGraphics.peekScissorStack()", "guiGraphics.scissorStack.peek()")
    }

    replacements.string(current.parsed >= "1.21.11") {
        replace("net.minecraft.resources.ResourceLocation", "net.minecraft.resources.Identifier")
        replace("renderer.RenderType", "renderer.rendertype.RenderType")
        replace("net.minecraft.Util", "net.minecraft.util.Util")
    }

    replacements.regex(current.parsed >= "1.21.11") {
        replace("\\bResourceLocation\\b" to "Identifier", "\\bIdentifier\\b" to "ResourceLocation")
    }

    // These renames all still hold on 26.3, so gate them on ">= 26.2" rather than an exact match.
    replacements.string(current.parsed >= "26.2") {
        replace("net.minecraft.client.gui.render.state", "net.minecraft.client.renderer.state.gui")
        replace("net.minecraft.client.renderer.state.LevelRenderState", "net.minecraft.client.renderer.state.level.LevelRenderState")
        replace(".getTags()", ".tags()")
        replace(".getItemHolder()", ".typeHolder()")
    }

    replacements.regex(current.parsed >= "26.2") {
        replace("\\bGuiGraphics\\b" to "GuiGraphicsExtractor", "\\bGuiGraphicsExtractor\\b" to "GuiGraphics")
    }

    // MC 26.3 moved the GPU abstraction layer out of com.mojang.blaze3d into the new
    // com.mojang.renderpearl library. PoseStack/VertexConsumer/DefaultVertexFormat/RenderSystem
    // stayed behind, so only VertexFormat needs remapping here.
    replacements.string(current.parsed >= "26.3") {
        replace("com.mojang.blaze3d.vertex.VertexFormat", "com.mojang.renderpearl.api.vertex.VertexFormat")
        // PoseStack.mulPose(Quaternionfc) became rotate(Quaternionfc); every mulPose call in this
        // mod passes a quaternion, so the blanket rename is safe here.
        replace(".mulPose(", ".rotate(")
    }

    replacements.string("ss_replacement", current.version.equals("1.20.1")) {
        replace("Styles.COMMON", "HelperMethods.getStyle(\"common\")")
        replace("Styles.UNIQUE", "HelperMethods.getStyle(\"unique\")")
        replace("Styles.LEGENDARY", "HelperMethods.getStyle(\"legendary\")")
        replace("Styles.RUNIC", "HelperMethods.getStyle(\"runic\")")

    }
}

tasks.named<Copy>("processResources") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}




// All dependencies should be specified through modstitch's proxy configuration.
// Wondering where the "repositories" block is? Go to "stonecutter.gradle.kts"
// If you want to create proxy configurations for more source sets, such as client source sets,
// use the modstitch.createProxyConfigurations(sourceSets["client"]) function.
dependencies {
    val loaderEnum = when {
        modstitch.isLoom -> Loaders.LOOM
        modstitch.isModDevGradleLegacy -> Loaders.FORGE
        modstitch.isModDevGradleRegular -> Loaders.NEOFORGE
        else -> throw IllegalArgumentException("unknown loader")
    }
    val fzzyConfigVersion = findProperty("deps.fzzy_config_version")
    val fzzyMinecraftVersion = when (minecraft) {
        "1.21.1" -> "1.21"
        "1.21.4" -> "1.21.3"
        "1.21.8" -> "1.21.6"
        "1.21.10" -> "1.21.9"
        else -> minecraft
    }
    var fzzyString : String = "";
    val libVersion = property("deps.lib_version") as String
    fun Dependency?.jij() = this?.also(::modstitchJiJ)
    fun String.implementation() = if (modstitch.isModDevGradleLegacy){
        add("modImplementation", this)
    } else {
        modstitchModImplementation(this)
    }
    fun String.runtimeOnly() = if (modstitch.isModDevGradleLegacy) {
        add("modRuntimeOnly", this)
    } else {
        modstitchModRuntimeOnly(this)
    }
    //fzzy
    modstitch.loom {
        val fabricApi = property("deps.fabric_api") as String
        modstitchModImplementation("net.fabricmc.fabric-api:fabric-api:${fabricApi}+${minecraft}")
        fzzyString = "me.fzzyhmstrs:fzzy_config:${fzzyConfigVersion}+${fzzyMinecraftVersion}";

    }

    modstitch.moddevgradle {
        if (modstitch.isModDevGradleLegacy){
            fzzyString = "me.fzzyhmstrs:fzzy_config:${fzzyConfigVersion}+${fzzyMinecraftVersion}+forge";
        } else {
            if (minecraft == "1.21.8"){
                fzzyString = "me.fzzyhmstrs:fzzy_config:${fzzyConfigVersion}+1.21.7+neoforge";
            } else {
                fzzyString = "me.fzzyhmstrs:fzzy_config:${fzzyConfigVersion}+${fzzyMinecraftVersion}+neoforge"
            }

        }

    }

    // The plain 0.7.7 builds for 26.3 have sliders and scroll bars that can't be dragged with the
    // mouse (26.3 changed input handling). The fix releases are only published on Modrinth, not on
    // fzzy_config's own Maven, so pull them from there.
    if (minecraft == "26.3") {
        fzzyString = "maven.modrinth:fzzy-config:0.7.7+fix2+26.3" + (if (modstitch.isLoom) "" else "+neoforge")
        // Modrinth serves the jar without fzzy_config's dependency metadata, so the Kotlin runtime it
        // normally pulls in transitively goes missing and fzzy can't load configs or open its screen.
        // Add back the Kotlin language provider mod that fzzy_config 0.7.7 declares for 26.3.
        if (modstitch.isLoom) {
            "net.fabricmc:fabric-language-kotlin:1.13.11+kotlin.2.3.21".runtimeOnly()
        } else {
            "dev.nyon:KotlinLangForge:2.14.1-k2.4.20-3.1+neoforge".runtimeOnly()
        }
    }

    modstitchModCompileOnly(fzzyString)
    (fzzyString).runtimeOnly()

    // No 26.x build of nirvana-library exists on Modrinth yet; consume the locally ported and
    // published builds (see NirvanaLib's own 26.2 and 26.3 ports) via mavenLocal instead. The
    // 26.3 publication is version-qualified so it does not overwrite the 26.2 one.
    if (minecraft == "26.3") {
        ("com.clefal:nirvana_lib:2.2.0+26.3").implementation()
    } else if (minecraft == "26.2") {
        ("com.clefal:nirvana_lib:2.2.0").implementation()
    } else {
        ("maven.modrinth:nirvana-library:${loader}-${minecraft}-${libVersion}").implementation()
    }
    // common-network has no 26.x build either, and it's unused directly in this mod's source
    // (runtimeOnly only), so it's simply omitted for those targets.
    if (minecraft != "26.2" && minecraft != "26.3") {
        ("maven.modrinth:common-network:${property("deps.common_network")}").runtimeOnly()
    }
    //loader-specified deps
    DependencyConfig.getDependencies(loaderEnum, minecraft).forEach { dep ->
        dependencies.add(dep.configuration, dep.notation, dep.options)
    }
    //lombok
    modstitchCompileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")

    testCompileOnly("org.projectlombok:lombok:1.18.42")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.42")

}

msPublishing {

    mpp {
        changelog = file("../../changelog.md")
            .readLines()
            .joinToString("\n") { line ->
                if (line.isNotBlank()) {
                    "$line</br>"
                } else {
                    line
                }
            }
        type = STABLE


        afterEvaluate {
            file = modstitch.finalJarTask.flatMap { it.archiveFile }
            this@mpp.displayName.set(file.map { it.asFile.name })
        }
        //dryRun = true
        if (file("D:\\curseforge-key.txt").exists()) {
            val cfOptions = curseforgeOptions {
                accessToken = file("D:\\curseforge-key.txt").readText()
                projectId = "1150640"
                minecraftVersions.add(minecraft)
                clientRequired = true
                serverRequired = false
                javaVersions.set(listOf(JavaVersion.toVersion(modstitch.javaVersion.get())))
                requires("nirvana-library")
            }
            curseforge("toCurseForge") {
                from(cfOptions)
            }
        }

        if (file("D:\\modrinth-key.txt").exists()) {
            // Modrinth options used by both Fabric and Forge
            val mrOptions = modrinthOptions {
                accessToken = file("D:\\modrinth-key.txt").readText()
                version = "${loader}-${minecraft}-${modstitch.metadata.modVersion.get()}"
                projectId = "rp7ooqvq"
                minecraftVersions.add(minecraft)
                requires("nirvana-library")
            }
            modrinth("toModrinth") {
                from(mrOptions)
            }
        }


    }

}
