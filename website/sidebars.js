// @ts-check

/** @type {import('@docusaurus/plugin-content-docs').SidebarsConfig} */
const sidebars = {
  mainSidebar: [
    'index',
    {
      type: 'category',
      label: 'Tutorials',
      link: {
        type: 'generated-index',
        title: 'Tutorials',
        description:
          'Step-by-step guides that teach you RegistryLib from scratch.',
        slug: '/tutorials',
      },
      items: [
        {
          type: 'category',
          label: 'Beginner',
          link: {
            type: 'generated-index',
            title: 'Beginner Tutorials',
            description: 'Get your first mod running in 15 minutes.',
            slug: '/tutorials/beginner',
          },
          items: [
            'tutorials/beginner/installation',
            'tutorials/beginner/first-item',
            'tutorials/beginner/first-block',
            'tutorials/beginner/understanding-chain',
          ],
        },
        {
          type: 'category',
          label: 'Intermediate',
          link: {
            type: 'generated-index',
            title: 'Intermediate Tutorials',
            description: 'Master efficient development patterns.',
            slug: '/tutorials/intermediate',
          },
          items: [
            'tutorials/intermediate/group-system',
            'tutorials/intermediate/tooltip-system',
            'tutorials/intermediate/multi-language',
            'tutorials/intermediate/recipes-tags',
          ],
        },
        {
          type: 'category',
          label: 'Expert',
          link: {
            type: 'generated-index',
            title: 'Expert Tutorials',
            description: 'Deep customization and extension.',
            slug: '/tutorials/expert',
          },
          items: [
            'tutorials/expert/custom-builder',
            'tutorials/expert/performance',
          ],
        },
      ],
    },
    {
      type: 'category',
      label: 'How-to Guides',
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
      ],
    },
    {
      type: 'category',
      label: 'Reference',
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
      items: ['faq', 'troubleshooting', 'glossary'],
    },
  ],
};

export default sidebars;
