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
                    message: '根据 <a href="https://www.apache.org/licenses/LICENSE-2.0" target="_blank">Apache License 2.0</a> 许可证发布',
                    copyright: 'Copyright © 2024 <a href="https://liuxp.me" target="_blank">liuxp.me</a>'
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
        ['meta', {name: 'author', content: 'liuxp'}],
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
        search: {
            provider: "algolia",
            options: {
                appId: "xxxx",
                apiKey: "xxx",
                indexName: "string",
                placeholder: "Search Doc",
            }
        },
        footer: {
            message: 'Publish under the <a href="https://www.apache.org/licenses/LICENSE-2.0" target="_blank">Apache License 2.0</a>',
            copyright: 'Copyright © 2024 <a href="https://liuxp.me" target="_blank">liuxp.me</a>'
        },
        nav: [
            {text: '指引', link: '/guide/intro', activeMatch: '/zh/guide/'},
            {text: '常见问题', link: '/faq/', activeMatch: '/zh/faq/'},
            {text: '捐赠', link: '/other/donate'},
        ],
        sidebar: {
            '/guide/': [
                {
                    text: '概述',
                    items: [
                        {
                            text: '首页',
                            link: '/guide/intro'
                        },
                        {
                            text: '更新日志',
                            link: '/guide/released'
                        }
                    ]
                },{
                    text: '用户手册',
                    items: [
                        {
                            text: '快速开始',
                            link: '/guide/user/quick-start'
                        },
                        {
                            text: 'API 使用',
                            link: '/guide/user/api'
                        },
                        {
                            text: '配置文件',
                            link: '/guide/user/config'
                        },
                        {
                            text: '非官方S3实现',
                            link: '/guide/user/custom'
                        },
                    ]
                },{
                    text: '开发者手册',
                    items: [
                        {
                            text: '开发计划',
                            link: '/guide/developers/plan'
                        },
                        {
                            text: '构建与运行',
                            link: '/guide/developers/building'
                        },
                        {
                            text: '代码结构',
                            link: '/guide/developers/framework'
                        },
                        {
                            text: '提交代码',
                            link: '/guide/developers/writing-code'
                        },
                        {
                            text: '编写文档',
                            link: '/guide/developers/writing-documents'
                        },
                        {
                            text: '贡献者列表',
                            link: '/guide/developers/contributors'
                        },
                    ]
                },{
                    text: '参考资料',
                    items: [
                        {
                            text: 'FAQ',
                            link: '/guide/references/faq'
                        },
                        {
                            text: 'MinIO S3 接口',
                            link: '/guide/references/minio-s3-api'
                        }
                    ]
                },{
                    text: 'MinIO 研究',
                    items: [
                        {
                            text: 'MinIO 分片 ETAG 生成机制',
                            link: '/guide/study/etag'
                        }
                    ]
                }
            ],
        }
    }
});
