import {defineConfig} from 'vitepress';

export default defineConfig({
    locales: {
        root: {
            label: '简体中文',
            lang: 'zh',
            dir: 'src/zh',
            title: 'MinIO-Plus',
            description: '成为 MinIO 最好的搭档'
        },
        en: {
            label: 'English',
            lang: 'en',
            dir: 'src/en',
            title: 'MinIO-Plus',
            description: '成为 MinIO 最好的搭档',
            themeConfig: {
                footer: {
                    message: '根据 MIT 许可证发布',
                    copyright: 'Copyright © 2024 lxp'
                },
                nav: [
                    {text: '指引', link: '/en/guide/intro', activeMatch: '/en/guide/'},
                    {text: '常见问题', link: '/en/faq/', activeMatch: '/en/faq/'},
                    {text: '捐赠', link: '/en/other/donate'},
                ],
                sidebar: {
                    '/en/guide/': [
                        {
                            text: 'Getting Started',
                            items: [
                                {
                                    text: 'Introduction',
                                    link: '/en/guide/intro'
                                },
                                {
                                    text: 'Quick Start',
                                    link: '/en/guide/quick-start'
                                }
                            ]
                        },
                    ],
                }
            }
        }
    },
    head: [
        ['meta', {name: 'author', content: 'lxp'}],
        [
            'meta',
            {
                name: 'keywords',
                content: 'minio, minio-plus minio tool'
            }
        ],
        ['link', {rel: 'icon', type: 'image/svg+xml', href: '/logo.svg'}],
        [
            'meta',
            {
                name: 'viewport',
                content: 'width=device-width,initial-scale=1,minimum-scale=1.0,maximum-scale=1.0,user-scalable=no'
            }
        ],
        ['link', {rel: 'icon', href: '/favicon.ico'}]
    ],
    assetsDir: 'public',
    srcDir: 'src',
    themeConfig: {
        logo: '/logo.svg',
        socialLinks: [
            {icon: 'github', link: 'https://github.com/lxp135/minio-plus'},

        ],
        algolia: {
            appId: 'sddsf',
            apiKey: 'sdfsdf',
            indexName: 'BaldHead'
        },
        footer: {
            message: 'Publish under the MIT license',
            copyright: 'Copyright © 2024 lxp'
        },
        nav: [
            {text: '指引', link: '/guide/intro', activeMatch: '/zh/guide/'},
            {text: '常见问题', link: '/faq/', activeMatch: '/zh/faq/'},
            {text: '捐赠', link: '/other/donate'},
        ],
        sidebar: {
            '/guide/': [
                {
                    text: '开始',
                    items: [
                        {
                            text: '简介',
                            link: '/guide/intro'
                        },
                        {
                            text: '快速上手',
                            link: '/guide/quick-start'
                        }
                    ]
                },
            ],
        }
    }
});
