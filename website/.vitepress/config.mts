import { defineConfig } from 'vitepress'
import { fileURLToPath } from 'node:url'

const vuePackages = new Set(['vue', 'vue/server-renderer'])

export default defineConfig({
  title: 'Java 从零到精通',
  description: '以 Java 21 为基线的系统化 Java 学习项目',
  base: '/java-from-zero-to-mastery/',
  srcDir: '../docs',
  outDir: './.vitepress/dist',
  rewrites: {
    'en/README.md': 'en/index.md'
  },
  vite: {
    plugins: [
      {
        name: 'resolve-vitepress-vue',
        enforce: 'pre',
        resolveId(source) {
          return vuePackages.has(source)
            ? fileURLToPath(import.meta.resolve(source))
            : null
        }
      }
    ]
  },
  locales: {
    root: {
      label: '简体中文',
      lang: 'zh-CN',
      link: '/'
    },
    en: {
      label: 'English',
      lang: 'en',
      link: '/en/'
    }
  },
  themeConfig: {
    i18nRouting: false,
    search: {
      provider: 'local'
    },
    nav: [
      { text: '首页', link: '/' },
      { text: '学习路线', link: '/zh-CN/roadmap/' },
      {
        text: '学习资源',
        items: [
          {
            text: '第一个 Java 程序',
            link: '/zh-CN/guide/01-getting-started/01-first-java-program'
          },
          {
            text: '开发环境与 IntelliJ IDEA',
            link: '/zh-CN/guide/01-getting-started/02-development-environment-and-idea'
          },
          {
            text: '变量与数据类型',
            link: '/zh-CN/guide/01-getting-started/03-variables-and-data-types'
          },
          {
            text: '流程控制',
            link: '/zh-CN/guide/01-getting-started/04-control-flow'
          },
          {
            text: '方法',
            link: '/zh-CN/guide/01-getting-started/05-methods'
          },
          {
            text: '数组',
            link: '/zh-CN/guide/01-getting-started/06-arrays'
          },
          {
            text: '字符串与 StringBuilder',
            link: '/zh-CN/guide/01-getting-started/07-strings-and-stringbuilder'
          },
          {
            text: 'IntelliJ IDEA 调试',
            link: '/zh-CN/guide/01-getting-started/08-debugging-with-intellij-idea'
          },
          {
            text: '类与对象',
            link: '/zh-CN/guide/02-java-core/01-classes-and-objects'
          },
          {
            text: '封装',
            link: '/zh-CN/guide/02-java-core/02-encapsulation'
          },
          {
            text: '继承与多态',
            link: '/zh-CN/guide/02-java-core/03-inheritance-and-polymorphism'
          },
          {
            text: '接口',
            link: '/zh-CN/guide/02-java-core/04-interfaces'
          },
          {
            text: '异常处理',
            link: '/zh-CN/guide/02-java-core/05-exceptions'
          },
          {
            text: '集合',
            link: '/zh-CN/guide/02-java-core/06-collections'
          },
          {
            text: '泛型',
            link: '/zh-CN/guide/02-java-core/07-generics'
          },
          {
            text: '文件 I/O',
            link: '/zh-CN/guide/02-java-core/08-file-io'
          },
          {
            text: '日期与时间 API',
            link: '/zh-CN/guide/02-java-core/09-date-time-api'
          },
          {
            text: 'Lambda 与 Stream API',
            link: '/zh-CN/guide/02-java-core/10-lambda-streams'
          },
          {
            text: '枚举与常用标准库',
            link: '/zh-CN/guide/02-java-core/11-enums-standard-library'
          },
          {
            text: 'Java 核心综合复盘',
            link: '/zh-CN/guide/02-java-core/12-java-core-review'
          },
          {
            text: '测试、Maven 与工程化验证',
            link: '/zh-CN/guide/03-engineering-foundation/01-testing-maven-ci'
          },
          { text: 'Java 手册', link: '/zh-CN/handbook/README' },
          { text: '面试与复习', link: '/zh-CN/interview/README' }
        ]
      },
      { text: '写作规范', link: '/zh-CN/writing-guide' },
      { text: 'English', link: '/en/' }
    ],
    sidebar: {
      '/zh-CN/': [
        {
          text: '开始学习',
          items: [
            { text: '学习路线', link: '/zh-CN/roadmap/' },
            {
              text: '第一个 Java 程序',
              link: '/zh-CN/guide/01-getting-started/01-first-java-program'
            },
            {
              text: '开发环境与 IntelliJ IDEA',
              link: '/zh-CN/guide/01-getting-started/02-development-environment-and-idea'
            },
            {
              text: '变量与数据类型',
              link: '/zh-CN/guide/01-getting-started/03-variables-and-data-types'
            },
            {
              text: '流程控制',
              link: '/zh-CN/guide/01-getting-started/04-control-flow'
            },
            {
              text: '方法',
              link: '/zh-CN/guide/01-getting-started/05-methods'
            },
            {
              text: '数组',
              link: '/zh-CN/guide/01-getting-started/06-arrays'
            },
            {
              text: '字符串与 StringBuilder',
              link: '/zh-CN/guide/01-getting-started/07-strings-and-stringbuilder'
            },
            {
              text: 'IntelliJ IDEA 调试',
              link: '/zh-CN/guide/01-getting-started/08-debugging-with-intellij-idea'
            },
            {
              text: '类与对象',
              link: '/zh-CN/guide/02-java-core/01-classes-and-objects'
            },
            {
              text: '封装',
              link: '/zh-CN/guide/02-java-core/02-encapsulation'
            },
            {
              text: '继承与多态',
              link: '/zh-CN/guide/02-java-core/03-inheritance-and-polymorphism'
            },
            {
              text: '接口',
              link: '/zh-CN/guide/02-java-core/04-interfaces'
            },
            {
              text: '异常处理',
              link: '/zh-CN/guide/02-java-core/05-exceptions'
            },
            {
              text: '集合',
              link: '/zh-CN/guide/02-java-core/06-collections'
            },
            {
              text: '泛型',
              link: '/zh-CN/guide/02-java-core/07-generics'
            },
            {
              text: '文件 I/O',
              link: '/zh-CN/guide/02-java-core/08-file-io'
            },
            {
              text: '日期与时间 API',
              link: '/zh-CN/guide/02-java-core/09-date-time-api'
            },
            {
              text: 'Lambda 与 Stream API',
              link: '/zh-CN/guide/02-java-core/10-lambda-streams'
            },
            {
              text: '枚举与常用标准库',
              link: '/zh-CN/guide/02-java-core/11-enums-standard-library'
            },
            {
              text: 'Java 核心综合复盘',
              link: '/zh-CN/guide/02-java-core/12-java-core-review'
            },
            {
              text: '测试、Maven 与工程化验证',
              link: '/zh-CN/guide/03-engineering-foundation/01-testing-maven-ci'
            }
          ]
        },
        {
          text: '参考资料',
          items: [
            { text: 'Java 手册', link: '/zh-CN/handbook/README' },
            { text: '面试与复习', link: '/zh-CN/interview/README' },
            { text: '写作规范', link: '/zh-CN/writing-guide' }
          ]
        }
      ]
    }
  }
})
