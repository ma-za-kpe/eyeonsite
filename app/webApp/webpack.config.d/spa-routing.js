// Enable SPA routing in the webpack dev server so /mazakpe and other
// paths serve index.html instead of returning 404.
if (config.devServer) {
    config.devServer.historyApiFallback = true;
}
