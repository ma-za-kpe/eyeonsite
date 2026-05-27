// Loader half of the Skiko WebGL patch. This file lives in webpack.config.d
// so Kotlin/JS also concatenates it into the generated webpack config. The
// guard keeps that config pass from replacing module.exports.

if (typeof config === "undefined") {
    module.exports = function patchSkikoLoader(source) {
        return source
            .split('return ver=="webgl"==gl instanceof WebGLRenderingContext?gl:null')
            .join("return gl")
            .split('var ctx=canvas.getContext("webgl2",webGLContextAttributes);if(!ctx)return 0;')
            .join('webGLContextAttributes.failIfMajorPerformanceCaveat=false;var ctx=canvas.getContext("webgl2",webGLContextAttributes);if(!ctx){throw new Error("EyeOnSite WebGL2 context unavailable")};');
    };
}
