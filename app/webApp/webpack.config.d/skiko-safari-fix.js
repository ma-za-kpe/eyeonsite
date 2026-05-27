// Patch Skiko's broken WebGL context guard at bundle time.
//
// Some Skiko builds emit:
//   return ver=="webgl"==gl instanceof WebGLRenderingContext?gl:null
//
// In Chrome, WebGL2RenderingContext is also a WebGLRenderingContext, so the
// chained comparison can return null for a valid WebGL2 context. Skiko then
// continues with context id 0 and later crashes in _glGetString because GLctx
// is undefined. The loader covers module transforms; the emit plugin covers
// eval-wrapped dev bundles and minified production bundles.

const fs = require("fs");
const path = require("path");
const webpack = require("webpack");

const replacements = [
    [
        'return ver=="webgl"==gl instanceof WebGLRenderingContext?gl:null',
        "return gl",
    ],
    [
        'return ver==\\"webgl\\"==gl instanceof WebGLRenderingContext?gl:null',
        "return gl",
    ],
    [
        'var ctx=canvas.getContext("webgl2",webGLContextAttributes);if(!ctx)return 0;',
        'webGLContextAttributes.failIfMajorPerformanceCaveat=false;var ctx=canvas.getContext("webgl2",webGLContextAttributes);if(!ctx){throw new Error("EyeOnSite WebGL2 context unavailable")};',
    ],
    [
        'var ctx=canvas.getContext(\\"webgl2\\",webGLContextAttributes);if(!ctx)return 0;',
        'webGLContextAttributes.failIfMajorPerformanceCaveat=false;var ctx=canvas.getContext(\\"webgl2\\",webGLContextAttributes);if(!ctx){throw new Error(\\"EyeOnSite WebGL2 context unavailable\\")};',
    ],
];

function patchSkikoSource(source) {
    return replacements.reduce(
        (patched, replacement) => patched.split(replacement[0]).join(replacement[1]),
        source,
    );
}

function patchSkikoFile(filePath) {
    if (!fs.existsSync(filePath)) return;

    const source = fs.readFileSync(filePath, "utf8");
    const patched = patchSkikoSource(source);
    if (patched !== source) {
        fs.writeFileSync(filePath, patched, "utf8");
    }
}

function patchGeneratedSkikoFiles() {
    [
        path.resolve(__dirname, "kotlin/skiko.mjs"),
        path.resolve(__dirname, "kotlin/skikod8.mjs"),
        path.resolve(__dirname, "../../../../app/webApp/build/processedResources/js/main/skiko.mjs"),
        path.resolve(__dirname, "../../../../app/webApp/build/processedResources/js/main/skikod8.mjs"),
    ].forEach(patchSkikoFile);
}

config.module.rules.push({
    test: /skiko\.mjs$/,
    loader: path.resolve(__dirname, "../../../../app/webApp/webpack.config.d/skiko-safari-fix-loader.js"),
    enforce: "post",
});

config.plugins.push({
    apply(compiler) {
        patchGeneratedSkikoFiles();
        compiler.hooks.beforeRun.tap("EyeOnSiteSkikoWebGLPatch", patchGeneratedSkikoFiles);
        compiler.hooks.watchRun.tap("EyeOnSiteSkikoWebGLPatch", patchGeneratedSkikoFiles);

        compiler.hooks.thisCompilation.tap("EyeOnSiteSkikoWebGLPatch", (compilation) => {
            compilation.hooks.processAssets.tap(
                {
                    name: "EyeOnSiteSkikoWebGLPatch",
                    stage: webpack.Compilation.PROCESS_ASSETS_STAGE_OPTIMIZE,
                },
                (assets) => {
                    Object.keys(assets)
                        .filter((assetName) => assetName.endsWith(".js"))
                        .forEach((assetName) => {
                            const source = assets[assetName].source().toString();
                            const patched = patchSkikoSource(source);
                            if (patched !== source) {
                                compilation.updateAsset(assetName, new webpack.sources.RawSource(patched));
                            }
                        });
                },
            );
        });
    },
});
