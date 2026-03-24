// @ts-check
import { themes as prismThemes } from 'prism-react-renderer';

/** @type {import('@docusaurus/types').Config} */
const config = {
  title: 'RegistryLib',
  tagline: 'Fluent registration for NeoForge — one chain call does it all.',
  favicon: 'img/favicon.ico',

  future: {
    v4: true,
  },

  url: 'https://registrylib.gtodyssey.com',
  baseUrl: '/',

  organizationName: 'GregTech-Odyssey',
  projectName: 'RegistryLib',
  trailingSlash: false,

  onBrokenLinks: 'throw',

  markdown: {
    hooks: {
      onBrokenMarkdownLinks: 'warn',
    },
  },

  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },

  presets: [
    [
      'classic',
      /** @type {import('@docusaurus/preset-classic').Options} */
      ({
        docs: {
          path: '../docs-content',
          routeBasePath: '/',
          sidebarPath: './sidebars.js',
          editUrl:
            'https://github.com/GregTech-Odyssey/RegistryLib/tree/26.1/docs-content/',
        },
        blog: false,
        theme: {
          customCss: './src/css/custom.css',
        },
      }),
    ],
  ],

  themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
    ({
      colorMode: {
        defaultMode: 'light',
        respectPrefersColorScheme: true,
      },
      docs: {
        sidebar: {
          hideable: true,
          autoCollapseCategories: true,
        },
      },
      navbar: {
        title: 'RegistryLib',
        items: [
          {
            type: 'docSidebar',
            sidebarId: 'mainSidebar',
            position: 'left',
            label: 'Docs',
          },
          {
            href: 'https://github.com/GregTech-Odyssey/RegistryLib',
            label: 'GitHub',
            position: 'right',
          },
        ],
      },
      footer: {
        style: 'dark',
        links: [
          {
            title: 'Learn',
            items: [
              { label: 'Getting Started', to: '/tutorials/installation' },
              { label: 'How-to Guides', to: '/how-to/register-items' },
            ],
          },
          {
            title: 'Reference',
            items: [
              { label: 'API Overview', to: '/reference/api-overview' },
              { label: 'Concepts', to: '/concepts/what-is-registrylib' },
            ],
          },
          {
            title: 'More',
            items: [
              {
                label: 'GitHub',
                href: 'https://github.com/GregTech-Odyssey/RegistryLib',
              },
            ],
          },
        ],
        copyright: `Copyright © ${new Date().getFullYear()} GregTech Odyssey. Built with Docusaurus.`,
      },
      prism: {
        theme: prismThemes.github,
        darkTheme: prismThemes.dracula,
        additionalLanguages: ['java', 'groovy', 'gradle', 'toml'],
      },
    }),

  plugins: [
    [
      '@docusaurus/plugin-client-redirects',
      {
        redirects: [
          // Old Jekyll URLs → new Docusaurus paths
          { from: '/quickstart', to: '/tutorials/installation' },
          { from: '/register-items', to: '/how-to/register-items' },
          { from: '/register-blocks', to: '/how-to/register-blocks' },
          { from: '/register-fluids-and-buckets', to: '/how-to/register-fluids' },
          {
            from: '/register-block-entities-and-renderers',
            to: '/how-to/register-block-entities',
          },
          { from: '/register-advancements', to: '/how-to/register-advancements' },
          { from: '/group-system', to: '/tutorials/group-system' },
          { from: '/tooltip-system', to: '/tutorials/tooltip-system' },
          { from: '/lang-system', to: '/tutorials/multi-language' },
          { from: '/api-reference', to: '/reference/api-overview' },
          { from: '/override-builders', to: '/tutorials/custom-builder' },
          { from: '/special-optimizations', to: '/tutorials/performance' },
          { from: '/content-guides', to: '/how-to/register-items' },
          { from: '/systems-overview', to: '/concepts/what-is-registrylib' },
          { from: '/advanced-topics', to: '/tutorials/custom-builder' },
          // Old 3-level tutorial URLs → flat URLs
          { from: '/tutorials/beginner/installation', to: '/tutorials/installation' },
          { from: '/tutorials/beginner/first-item', to: '/tutorials/first-item' },
          { from: '/tutorials/beginner/first-block', to: '/tutorials/first-block' },
          { from: '/tutorials/beginner/understanding-chain', to: '/tutorials/understanding-chain' },
          { from: '/tutorials/intermediate/group-system', to: '/tutorials/group-system' },
          { from: '/tutorials/intermediate/tooltip-system', to: '/tutorials/tooltip-system' },
          { from: '/tutorials/intermediate/multi-language', to: '/tutorials/multi-language' },
          { from: '/tutorials/intermediate/recipes-tags', to: '/tutorials/recipes-tags' },
          { from: '/tutorials/expert/custom-builder', to: '/tutorials/custom-builder' },
          { from: '/tutorials/expert/performance', to: '/tutorials/performance' },
        ],
      },
    ],
  ],
};

export default config;
