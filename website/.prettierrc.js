/** @type {import("prettier").Config} */
const config = {
    printWidth: 120,
    tabWidth: 4,
    trailingComma: 'all',
    semi: true,
    jsxSingleQuote: true,
    singleQuote: true,
    quoteProps: 'as-needed',
    plugins: ['prettier-plugin-astro', 'prettier-plugin-tailwindcss'],
};
export default config;
