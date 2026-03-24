// @ts-check

/** @type {import('@docusaurus/plugin-content-docs').SidebarsConfig} */
const sidebars = {
  mainSidebar: [
    'index',
    {
      type: 'category',
      label: 'Tutorials',
      collapsible: false,
      link: {
        type: 'generated-index',
        title: 'Tutorials',
        description:
          'Step-by-step guides that teach you RegistryLib from scratch.',
        slug: '/tutorials',
      },
      items: [
        'tutorials/installation',
        'tutorials/first-item',
        'tutorials/first-block',
        'tutorials/understanding-chain',
        'tutorials/group-system',
        'tutorials/tooltip-system',
        'tutorials/multi-language',
        'tutorials/recipes-tags',
        'tutorials/custom-builder',
        'tutorials/performance',
      ],
    },
    {
      type: 'category',
      label: 'How-to Guides',
      collapsible: false,
      link: {
        type: 'generated-index',
        title: 'How-to Guides',
        description:
          'Task-oriented guides for specific registration goals.',
        slug: '/how-to',
      },
      items: [
        'how-to/register-items',
        'how-to/register-blocks',
        'how-to/register-fluids',
        'how-to/register-block-entities',
        'how-to/register-advancements',
        'how-to/register-recipes',
        'how-to/register-custom-ingredients',
        'how-to/register-enchantments',
      ],
    },
    {
      type: 'category',
      label: 'Reference',
      collapsible: false,
      link: {
        type: 'generated-index',
        title: 'API Reference',
        description: 'Quick-lookup tables for the RegistryLib API.',
        slug: '/reference',
      },
      items: [
        'reference/api-overview',
        'reference/entry-types',
        'reference/builder-methods',
      ],
    },
    {
      type: 'category',
      label: 'Concepts',
      collapsible: false,
      link: {
        type: 'generated-index',
        title: 'Core Concepts',
        description:
          'Understand the design principles behind RegistryLib.',
        slug: '/concepts',
      },
      items: [
        'concepts/what-is-registrylib',
        'concepts/builder-pattern',
        'concepts/design-decisions',
      ],
    },
    {
      type: 'category',
      label: 'Help',
      collapsible: false,
      items: ['faq', 'troubleshooting', 'glossary', 'acknowledgements'],
    },
  ],
};

export default sidebars;
