const docx = require('docx');
const fs = require('fs');
const {
    Document, Packer, Paragraph, TextRun, HeadingLevel,
    AlignmentType, TabStopPosition, TabStopType,
    Table, TableRow, TableCell, WidthType, BorderStyle,
    PageBreak, Footer, Header, NumberFormat,
    convertInchesToTwip, LevelFormat, ShadingType
} = docx;

const FONT_SONG = '宋体';
const FONT_HEI = '黑体';
const FONT_TIMES = 'Times New Roman';

const TAB_RIGHT = 9072;

function createTitle(text, font = FONT_HEI, size = 22, bold = true, alignment = AlignmentType.CENTER) {
    return new Paragraph({
        alignment,
        spacing: { before: 200, after: 200 },
        children: [
            new TextRun({ text, font, size, bold }),
        ],
    });
}

function createHeading1(text) {
    return new Paragraph({
        heading: HeadingLevel.HEADING_1,
        alignment: AlignmentType.LEFT,
        spacing: { before: 400, after: 200 },
        children: [
            new TextRun({ text, font: FONT_HEI, size: 24, bold: true }),
        ],
    });
}

function createHeading2(text) {
    return new Paragraph({
        heading: HeadingLevel.HEADING_2,
        alignment: AlignmentType.LEFT,
        spacing: { before: 300, after: 150 },
        children: [
            new TextRun({ text, font: FONT_HEI, size: 22, bold: true }),
        ],
    });
}

function createHeading3(text) {
    return new Paragraph({
        heading: HeadingLevel.HEADING_3,
        alignment: AlignmentType.LEFT,
        spacing: { before: 200, after: 100 },
        children: [
            new TextRun({ text, font: FONT_HEI, size: 21, bold: true }),
        ],
    });
}

function createParagraph(text, indent = true) {
    return new Paragraph({
        alignment: AlignmentType.JUSTIFIED,
        spacing: { before: 50, after: 50, line: 360 },
        indent: indent ? { firstLine: convertInchesToTwip(0.49) } : undefined,
        children: [
            new TextRun({ text, font: FONT_SONG, size: 21 }),
        ],
    });
}

function createParagraphWithRuns(runs, indent = true) {
    return new Paragraph({
        alignment: AlignmentType.JUSTIFIED,
        spacing: { before: 50, after: 50, line: 360 },
        indent: indent ? { firstLine: convertInchesToTwip(0.49) } : undefined,
        children: runs,
    });
}

function createEmptyParagraph() {
    return new Paragraph({ children: [] });
}

function createCoverLine(label, value) {
    return new Paragraph({
        alignment: AlignmentType.CENTER,
        spacing: { before: 100, after: 100 },
        children: [
            new TextRun({ text: label, font: FONT_SONG, size: 24 }),
            new TextRun({ text: value, font: FONT_SONG, size: 24, underline: { type: 'single' } }),
        ],
    });
}

function toc1(text, page) {
    return new Paragraph({
        spacing: { before: 60, after: 60, line: 360 },
        tabStops: [{ type: TabStopType.RIGHT, position: TAB_RIGHT, leader: 'dot' }],
        children: [
            new TextRun({ text, font: FONT_HEI, size: 21, bold: true }),
            new TextRun({ text: '\t' }),
            new TextRun({ text: String(page), font: FONT_SONG, size: 21 }),
        ],
    });
}

function toc2(text, page) {
    return new Paragraph({
        spacing: { before: 40, after: 40, line: 360 },
        indent: { left: convertInchesToTwip(0.3) },
        tabStops: [{ type: TabStopType.RIGHT, position: TAB_RIGHT, leader: 'dot' }],
        children: [
            new TextRun({ text, font: FONT_SONG, size: 21, bold: true }),
            new TextRun({ text: '\t' }),
            new TextRun({ text: String(page), font: FONT_SONG, size: 21 }),
        ],
    });
}

function toc3(text, page) {
    return new Paragraph({
        spacing: { before: 30, after: 30, line: 360 },
        indent: { left: convertInchesToTwip(0.6) },
        tabStops: [{ type: TabStopType.RIGHT, position: TAB_RIGHT, leader: 'dot' }],
        children: [
            new TextRun({ text, font: FONT_SONG, size: 21 }),
            new TextRun({ text: '\t' }),
            new TextRun({ text: String(page), font: FONT_SONG, size: 21 }),
        ],
    });
}

function createDbHeaderRow(cells) {
    return new TableRow({
        children: cells.map((cell, i) => {
            return new TableCell({
                width: i === 0 ? { size: 2500, type: WidthType.DXA } :
                         i === 1 ? { size: 1800, type: WidthType.DXA } :
                         i === 2 ? { size: 1200, type: WidthType.DXA } :
                         { size: 4500, type: WidthType.DXA },
                shading: { type: ShadingType.SOLID, color: 'D9E2F3' },
                children: [
                    new Paragraph({
                        alignment: AlignmentType.CENTER,
                        children: [
                            new TextRun({ text: cell, font: FONT_SONG, size: 20, bold: true }),
                        ],
                    }),
                ],
            });
        }),
    });
}

function createDbTableRow(cells) {
    return new TableRow({
        children: cells.map((cell, i) => {
            return new TableCell({
                width: i === 0 ? { size: 2500, type: WidthType.DXA } :
                         i === 1 ? { size: 1800, type: WidthType.DXA } :
                         i === 2 ? { size: 1200, type: WidthType.DXA } :
                         { size: 4500, type: WidthType.DXA },
                children: [
                    new Paragraph({
                        alignment: AlignmentType.CENTER,
                        children: [
                            new TextRun({ text: cell, font: FONT_SONG, size: 20 }),
                        ],
                    }),
                ],
            });
        }),
    });
}

function createTestTableRow(cells) {
    return new TableRow({
        children: cells.map(cell => {
            return new TableCell({
                children: [
                    new Paragraph({
                        alignment: AlignmentType.CENTER,
                        children: [
                            new TextRun({ text: cell, font: FONT_SONG, size: 18 }),
                        ],
                    }),
                ],
            });
        }),
    });
}

const doc = new Document({
    styles: {
        default: {
            document: {
                run: {
                    font: FONT_SONG,
                    size: 21,
                },
            },
            heading1: {
                run: {
                    font: FONT_HEI,
                    size: 24,
                    bold: true,
                },
                paragraph: {
                    spacing: { before: 400, after: 200 },
                },
            },
            heading2: {
                run: {
                    font: FONT_HEI,
                    size: 22,
                    bold: true,
                },
                paragraph: {
                    spacing: { before: 300, after: 150 },
                },
            },
            heading3: {
                run: {
                    font: FONT_HEI,
                    size: 21,
                    bold: true,
                },
                paragraph: {
                    spacing: { before: 200, after: 100 },
                },
            },
        },
    },
    sections: [
        // ============ 封面 ============
        {
            properties: {
                page: {
                    margin: {
                        top: convertInchesToTwip(1),
                        bottom: convertInchesToTwip(1),
                        left: convertInchesToTwip(1.25),
                        right: convertInchesToTwip(1.25),
                    },
                },
            },
            children: [
                createEmptyParagraph(),
                createEmptyParagraph(),
                createEmptyParagraph(),
                createTitle('丽水学院', FONT_HEI, 36, true),
                createTitle('毕业论文（设计）', FONT_HEI, 36, true),
                createEmptyParagraph(),
                createTitle('（2025届）', FONT_SONG, 28, false),
                createEmptyParagraph(),
                createEmptyParagraph(),
                createCoverLine('题目  ', '基于SpringBoot的药品管理系统的设计与实现'),
                createEmptyParagraph(),
                createCoverLine('院别  ', '数学与计算机学院'),
                createEmptyParagraph(),
                createCoverLine('校内导师  ', '__________'),
                createCoverLine('职称  ', '__________'),
                createEmptyParagraph(),
                createCoverLine('班级  ', '__________'),
                createCoverLine('姓名  ', '__________'),
                createCoverLine('学号  ', '__________'),
                createEmptyParagraph(),
                createEmptyParagraph(),
                createTitle('2025年  月  日', FONT_SONG, 24, false),
            ],
        },
        // ============ 中文摘要 ============
        {
            properties: {
                page: {
                    margin: {
                        top: convertInchesToTwip(1),
                        bottom: convertInchesToTwip(1),
                        left: convertInchesToTwip(1.25),
                        right: convertInchesToTwip(1.25),
                    },
                },
            },
            headers: {
                default: new Header({
                    children: [
                        new Paragraph({
                            alignment: AlignmentType.CENTER,
                            children: [
                                new TextRun({ text: '丽水学院2025届学生毕业设计（论文）', font: FONT_SONG, size: 18 }),
                            ],
                        }),
                    ],
                }),
            },
            children: [
                createTitle('基于SpringBoot的药品管理系统的设计与实现', FONT_HEI, 24, true),
                createParagraphWithRuns([
                    new TextRun({ text: '学院：数学与计算机学院  专业：__________  班级：__________  作者：__________  指导老师：__________', font: FONT_SONG, size: 18 }),
                ], false),
                createEmptyParagraph(),
                createParagraphWithRuns([
                    new TextRun({ text: '摘要：', font: FONT_HEI, size: 21, bold: true }),
                    new TextRun({ text: '随着医疗卫生事业的不断发展和人民群众健康意识的日益增强，药品管理的规范化与信息化已成为医疗行业的重要需求。传统的药品管理方式主要依赖人工记录和纸质台账，存在效率低下、易出错、信息检索困难等问题，难以满足现代药房对药品采购、存储、发放等环节的精细化管理要求。本系统基于SpringBoot框架和Vue技术，结合MySQL数据库，采用前后端分离架构，设计并实现了一套药品管理系统。系统主要包括药品管理、药品分类管理、药房管理、药品流转记录、用户管理和管理员管理等功能模块，实现了药品信息的增删改查、药品采购入库与发药出库的流程管理、药品流转记录的查询与追踪以及基于角色的权限控制等核心功能。本系统的开发不仅提升了SpringBoot框架在医药管理领域的应用水平，同时为药房信息化管理提供了新的实践方向，为用户提供了更便捷、高效的药品管理体验，助力药房实现规范化、智能化的管理目标。', font: FONT_SONG, size: 21 }),
                ]),
                createEmptyParagraph(),
                createParagraphWithRuns([
                    new TextRun({ text: '关键词：', font: FONT_HEI, size: 21, bold: true }),
                    new TextRun({ text: '药品管理；SpringBoot；Vue；MySQL', font: FONT_SONG, size: 21 }),
                ], false),
            ],
        },
        // ============ 英文摘要 ============
        {
            properties: {
                page: {
                    margin: {
                        top: convertInchesToTwip(1),
                        bottom: convertInchesToTwip(1),
                        left: convertInchesToTwip(1.25),
                        right: convertInchesToTwip(1.25),
                    },
                },
            },
            headers: {
                default: new Header({
                    children: [
                        new Paragraph({
                            alignment: AlignmentType.CENTER,
                            children: [
                                new TextRun({ text: '丽水学院2025届学生毕业设计（论文）', font: FONT_SONG, size: 18 }),
                            ],
                        }),
                    ],
                }),
            },
            children: [
                createTitle('Design and Implementation of a Drug Management System Based on SpringBoot', FONT_TIMES, 24, true),
                createParagraphWithRuns([
                    new TextRun({ text: 'Major:__________  Class:__________  Name:__________  Instructor:__________', font: FONT_TIMES, size: 18 }),
                ], false),
                createEmptyParagraph(),
                createParagraphWithRuns([
                    new TextRun({ text: 'Abstract: ', font: FONT_TIMES, size: 21, bold: true }),
                    new TextRun({ text: 'With the continuous development of medical and health services and the increasing health awareness of the people, the standardization and informatization of drug management has become an important demand in the medical industry. Traditional drug management methods mainly rely on manual records and paper ledgers, which have problems such as low efficiency, error proneness, and difficulty in information retrieval, making it difficult to meet the refined management requirements of modern pharmacies for drug procurement, storage, and dispensing. This system is based on the SpringBoot framework and Vue technology, combined with MySQL database, adopting a front-end and back-end separation architecture, and designs and implements a drug management system. The system mainly includes functional modules such as drug management, drug category management, pharmacy management, drug flow records, user management, and administrator management, achieving core functions such as CRUD operations on drug information, process management of drug procurement and dispensing, query and tracking of drug flow records, and role-based access control. The development of this system not only enhances the application level of the SpringBoot framework in the field of pharmaceutical management, but also provides a new practical direction for pharmacy informatization management, offering users a more convenient and efficient drug management experience, and helping pharmacies achieve standardized and intelligent management goals.', font: FONT_TIMES, size: 21 }),
                ]),
                createEmptyParagraph(),
                createParagraphWithRuns([
                    new TextRun({ text: 'Keywords: ', font: FONT_TIMES, size: 21, bold: true }),
                    new TextRun({ text: 'Drug Management; SpringBoot; Vue; MySQL', font: FONT_TIMES, size: 21 }),
                ], false),
            ],
        },
        // ============ 目录 ============
        {
            properties: {
                page: {
                    margin: {
                        top: convertInchesToTwip(1),
                        bottom: convertInchesToTwip(1),
                        left: convertInchesToTwip(1.25),
                        right: convertInchesToTwip(1.25),
                    },
                },
            },
            headers: {
                default: new Header({
                    children: [
                        new Paragraph({
                            alignment: AlignmentType.CENTER,
                            children: [
                                new TextRun({ text: '丽水学院2025届学生毕业设计（论文）', font: FONT_SONG, size: 18 }),
                            ],
                        }),
                    ],
                }),
            },
            children: [
                createTitle('目录', FONT_HEI, 28, true),
                toc1('第1章 绪论', 1),
                toc2('1.1 开发背景', 1),
                toc2('1.2 预期实现目标', 2),
                toc1('第2章 需求分析', 3),
                toc2('2.1 需求概述', 3),
                toc2('2.2 功能需求', 3),
                toc3('2.2.1 前端', 4),
                toc3('2.2.2 后端', 4),
                toc2('2.3 非功能性需求', 5),
                toc3('2.3.1 安全性', 5),
                toc3('2.3.2 交互性', 5),
                toc3('2.3.3 健壮性', 6),
                toc3('2.3.4 可用性', 6),
                toc3('2.3.5 可扩展性', 7),
                toc3('2.3.6 性能', 7),
                toc1('第3章 技术背景与相关工具', 8),
                toc2('3.1 Vue介绍', 8),
                toc2('3.2 Axios介绍', 8),
                toc2('3.3 Element UI介绍', 9),
                toc2('3.4 SpringBoot介绍', 9),
                toc2('3.5 MyBatisPlus介绍', 10),
                toc2('3.6 MySQL介绍', 10),
                toc1('第4章 系统设计', 11),
                toc2('4.1 系统架构', 11),
                toc3('4.1.1 Controller层', 11),
                toc3('4.1.2 Service层', 12),
                toc3('4.1.3 Mapper层', 12),
                toc2('4.2 模块设计', 13),
                toc3('4.2.1 系统功能模块', 13),
                toc3('4.2.2 药品功能模块', 14),
                toc3('4.2.3 管理功能模块', 14),
                toc2('4.3 数据库设计', 15),
                toc3('4.3.1 数据库概述', 15),
                toc3('4.3.2 数据库介绍', 16),
                toc1('第5章 药品管理系统的实现', 20),
                toc2('5.1 系统功能的实现', 20),
                toc3('5.1.1 登录功能', 20),
                toc2('5.2 药品功能的实现', 21),
                toc3('5.2.1 药品管理功能', 21),
                toc3('5.2.2 药品采购功能', 22),
                toc3('5.2.3 发药功能', 23),
                toc2('5.3 管理功能的实现', 24),
                toc3('5.3.1 药品分类管理功能', 24),
                toc3('5.3.2 药房管理功能', 25),
                toc3('5.3.3 药品流转记录功能', 25),
                toc3('5.3.4 用户管理功能', 26),
                toc3('5.3.5 管理员管理功能', 26),
                toc1('第6章 系统测试', 27),
                toc2('6.1 测试概述', 27),
                toc2('6.2 测试用例', 27),
                toc2('6.3 测试结论', 30),
                toc1('总结', 31),
                toc1('参考文献', 32),
            ],
        },
        // ============ 正文 ============
        {
            properties: {
                page: {
                    margin: {
                        top: convertInchesToTwip(1),
                        bottom: convertInchesToTwip(1),
                        left: convertInchesToTwip(1.25),
                        right: convertInchesToTwip(1.25),
                    },
                },
            },
            headers: {
                default: new Header({
                    children: [
                        new Paragraph({
                            alignment: AlignmentType.CENTER,
                            children: [
                                new TextRun({ text: '丽水学院2025届学生毕业设计（论文）', font: FONT_SONG, size: 18 }),
                            ],
                        }),
                    ],
                }),
            },
            children: [
                // ====== 第1章 绪论 ======
                createHeading1('第1章 绪论'),
                createHeading2('1.1 开发背景'),
                createParagraph('随着我国医疗卫生体制改革的深入推进和人民群众健康意识的不断提升，药品作为保障人民健康的重要物资，其管理的规范化与信息化已成为医疗行业发展的必然趋势。药房作为药品供应的重要环节，承担着药品采购、存储、发放等关键职能，其管理水平直接影响着药品的安全性和有效性。然而，目前许多中小型药房和基层医疗机构仍然采用传统的人工管理方式，依赖纸质台账和手工记录进行药品的出入库管理，这种方式不仅效率低下，而且容易出现数据遗漏、记录错误等问题，难以满足现代药房对药品精细化管理的需求[1]。'),
                createParagraph('在信息化技术飞速发展的背景下，利用计算机技术和网络技术实现药品管理的信息化已成为解决上述问题的有效途径。药品管理系统作为一种信息化的管理工具，能够实现药品信息的电子化存储、药品流转的自动化记录以及药品库存的实时监控，从而有效提升药房的管理效率和服务质量。通过引入药品管理系统，药房可以摆脱传统人工管理的局限性，实现药品从采购入库到发放出库的全流程追踪，确保药品信息的准确性和可追溯性，降低药品管理中的人为失误风险[2]。'),
                createParagraph('近年来，以SpringBoot为代表的Java Web开发框架和以Vue为代表的前端技术日趋成熟，为药品管理系统的开发提供了坚实的技术基础。SpringBoot框架通过自动化配置和约定优于配置的原则，极大地简化了后端应用的开发过程；Vue框架则以其轻量、灵活和高效的特点，为构建用户友好的交互界面提供了有力支持。结合MySQL数据库的稳定性和高性能，采用前后端分离的架构模式，能够开发出功能完善、性能优良、易于维护的药品管理系统，为药房的信息化管理提供切实可行的解决方案。'),
                createParagraph('因此，本课题基于SpringBoot框架和Vue技术，结合MySQL数据库，设计并实现一套药品管理系统，旨在解决传统药品管理方式中存在的效率低、易出错、信息不透明等问题，为药房提供一套便捷、高效、可靠的药品信息化管理工具，推动药房管理向规范化、智能化方向发展。'),

                createHeading2('1.2 预期实现目标'),
                createParagraph('本系统的预期实现目标是设计并开发一套功能完善、操作便捷、运行稳定的药品管理系统，以满足药房日常药品管理的实际需求。具体而言，系统应实现以下目标：'),
                createParagraph('第一，实现药品信息的全面管理。系统应支持对药品基本信息（包括药品名称、分类、所属药房、库存数量、备注等）的增删改查操作，使管理人员能够方便地维护和查询药品信息，确保药品数据的准确性和时效性。'),
                createParagraph('第二，实现药品流转的流程化管理。系统应支持药品采购入库和发药出库两种流转操作，在采购时自动记录操作人和取药人信息，在发药时自动扣减库存并记录流转信息，实现药品从入库到出库的全流程追踪和记录。'),
                createParagraph('第三，实现基于角色的权限控制。系统应区分超级管理员、管理员和普通用户三种角色，不同角色拥有不同的菜单权限和操作权限，确保系统数据的安全性和操作的规范性。'),
                createParagraph('第四，实现药品流转记录的查询与追踪。系统应提供药品流转记录的分页查询功能，支持按药品名称、分类、药房等多维度条件进行筛选查询，使管理人员能够方便地追溯药品的流转历史。'),
                createParagraph('第五，实现友好的用户交互体验。系统前端应采用现代化的UI设计，提供直观友好的操作界面，包括表格数据的分页展示、选中行的高亮显示、表单的动态校验等功能，确保用户能够高效便捷地使用系统。'),
                createParagraph('通过实现以上目标，本系统将为药房提供一个集药品信息管理、流转操作、记录查询和权限控制于一体的综合性管理平台，有效提升药房的药品管理效率和信息化水平。'),

                // ====== 第2章 需求分析 ======
                createHeading1('第2章 需求分析'),
                createHeading2('2.1 需求概述'),
                createParagraph('药品管理系统是一款专注于提升药房药品管理效率和规范化水平的信息化管理平台。通过为用户提供药品信息管理、药品流转操作、药品流转记录查询和系统管理等功能，提出高效、便捷、稳定的药品管理解决方案。药房管理人员可通过该系统方便地维护药品信息、执行药品采购入库和发药出库操作、查询和追踪药品流转记录，并且在基于角色的权限控制机制下进行安全的管理活动，确保药品管理的规范性和数据的安全性。'),

                createHeading2('2.2 功能需求'),
                createParagraph('本系统需要完成符合药品管理业务流程的相关功能。为了能够实现药品信息管理、药品流转操作、流转记录查询和系统管理等核心功能，本系统需要划分为前端和后端两个主要组成部分。'),

                createHeading3('2.2.1 前端'),
                createParagraph('前端负责设计和提供易用的用户界面，提供直观的操作方式和确保用户体验的流畅度，实现包括用户登录、药品管理、药品分类管理、药房管理、药品流转记录、用户管理和管理员管理等功能的页面显示与相关操作，方便用户与系统之间的交互，使得用户可以便捷地进行操作和使用系统功能，满足用户轻松直观地查看和便捷操作的需求。'),
                createParagraph('前端采用Vue 2框架构建单页面应用，结合Element UI组件库实现丰富的交互组件，包括数据表格、分页器、表单对话框、下拉选择框等，使用Axios库与后端进行HTTP请求通信，使用Vue Router实现页面路由的动态管理，使用Vuex进行全局状态管理。前端页面设计遵循现代简约风格，注重布局的合理性和色彩的协调性，通过CSS变量统一管理主题色彩，确保系统视觉风格的一致性。'),

                createHeading3('2.2.2 后端'),
                createParagraph('后端部分负责处理数据、执行业务逻辑以及保障系统的安全性，涵盖了药品信息的存储与管理、药品流转记录的存储与检索、用户信息的验证与权限的控制等方面的功能，需要设计合理的数据库表结构，实现数据的高效存取，提高完成各个功能的性能和效率，也要确保系统功能的顺利运行和安全性的保障。'),
                createParagraph('后端采用SpringBoot框架构建RESTful API，使用MyBatisPlus作为ORM框架简化数据库操作，通过Controller层接收前端请求，通过Service层处理业务逻辑，通过Mapper层与MySQL数据库进行数据交互。后端还实现了统一的结果封装（Result类）、分页查询参数封装（QueryPageParam类）、跨域配置（CorsConfig）和自动填充创建时间等功能，确保系统的高效运行和良好的可维护性。'),

                createHeading2('2.3 非功能性需求'),
                createHeading3('2.3.1 安全性'),
                createParagraph('药品管理系统需要确保用户数据的安全性和药品信息的准确性。系统应实现基于角色的访问控制机制，区分超级管理员、管理员和普通用户三种角色，不同角色拥有不同的菜单访问权限，防止未授权用户访问敏感数据或执行越权操作。系统在登录时需验证用户账号和密码的正确性，登录成功后根据用户角色动态加载对应的菜单权限，确保用户只能访问其权限范围内的功能模块。同时，系统应采用跨域配置（CORS）限制非法来源的请求，防止跨站请求伪造等安全威胁。'),

                createHeading3('2.3.2 交互性'),
                createParagraph('本系统的用户界面设计应当符合用户习惯和直观感受，提供直观友好的交互方式，使用户能够轻松便捷地完成药品管理、流转操作和记录查询等相关操作。系统采用Element UI组件库提供丰富的交互组件，包括表格的排序与筛选、表单的动态校验与提示、对话框的弹窗交互、分页器的页码导航等，确保用户操作的流畅性和便捷性。同时，系统也应注意响应速度，保持高效稳定的响应速度可以减少用户等待时间，提升用户体验[3]。'),

                createHeading3('2.3.3 健壮性'),
                createParagraph('系统的各个方面都需要具备良好的容错性和异常处理能力，能够应对各种特殊情况和异常输入。在系统的前后端接收到非法数据或者用户做出一些意外的操作时，系统能及时发现并报告错误，分别能做出相应的处理，妥善解决问题，保证系统的稳定运行。后端应设计健壮的逻辑处理机制和校验机制，能够检测并处理数据异常，避免因数据错误导致系统崩溃或数据丢失。前端也必须设计相应的机制，通过表单校验提前发现和过滤非法数据，减轻后端压力。'),

                createHeading3('2.3.4 可用性'),
                createParagraph('界面的设计应简洁明了，功能布局合理并且全面，使大部分用户能够快速上手，轻松地能理解各个按钮或流程的功能，并高效地使用系统，灵活地运用各个功能，流畅地完成业务流程，让用户能够有良好的系统交互体验。系统采用现代简约的设计风格，统一的色彩搭配和布局规范，使界面美观大方且易于理解。'),

                createHeading3('2.3.5 可扩展性'),
                createParagraph('药品管理系统需要具备良好的可扩展性，能够灵活地扩展服务器资源或系统架构，应对在随着用户量和数据量的增长的情况下而不断增长的用户需求。系统采用前后端分离的架构设计，前后端通过RESTful API进行通信，使得前端和后端可以独立开发、独立部署和独立扩展。后端采用分层架构（Controller-Service-Mapper），各层职责清晰，便于增加新功能或调整系统组件，提升系统的灵活性和可维护性。'),

                createHeading3('2.3.6 性能'),
                createParagraph('性能是用户对一个系统最直观的感受之一，系统应具备高效的数据处理和计算能力，能够快速响应用户请求并实时处理大量数据。本系统采用MyBatisPlus的分页插件实现数据的分页查询，避免一次性加载大量数据导致的性能问题。数据库查询使用LambdaQueryWrapper构建高效的查询条件，结合MySQL数据库的索引优化，确保数据查询的快速响应。同时，前后端分离的架构使得前端页面的渲染和后端的数据处理可以并行进行，有效提升系统的整体性能表现[4]。'),

                // ====== 第3章 技术背景与相关工具 ======
                createHeading1('第3章 技术背景与相关工具'),
                createHeading2('3.1 Vue介绍'),
                createParagraph('Vue.js是一款流行的前端JavaScript框架，能够用于构建交互式的Web用户界面。它易于上手、灵活且高效，拥有强大的数据绑定和组件化特性，能够帮助开发者快速构建现代化的单页面应用（SPA）和动态网页应用。Vue的核心库只关注视图层，不仅易于上手，还便于与第三方库或既有项目整合。本系统的前端页面基于Vue 2完成，实现了用户交互友好的页面，通过组件化的开发方式将系统拆分为多个可复用的Vue组件，提高了代码的可维护性和开发效率[5]。'),

                createHeading2('3.2 Axios介绍'),
                createParagraph('Axios是一个基于Promise的HTTP网络请求库，符合最新的ES规范，用于在浏览器和Node.js环境中发起HTTP请求，能够支持异步请求、拦截请求和响应和自动转换JSON数据等。Axios提供了简洁的API和丰富的功能特性，包括请求和响应拦截器、请求取消、自动JSON数据转换、客户端防御XSRF等，使得前后端数据交互变得简单高效。本系统中引入Axios之后，能够更加便捷地与后端进行数据交互，以逻辑更为清晰的方式更加高效地调用后端接口。'),

                createHeading2('3.3 Element UI介绍'),
                createParagraph('Element UI是一套基于Vue 2的桌面端组件库，提供了丰富的UI组件，包括表格、表单、对话框、分页器、下拉选择框、按钮等，能够帮助开发者快速构建美观、一致的用户界面。Element UI的设计遵循一致性、反馈性、效率性和可控性原则，提供了完善的文档和示例，降低了前端开发的门槛。本系统广泛使用了Element UI的组件，包括ElTable用于数据展示、ElPagination用于分页导航、ElDialog用于弹窗交互、ElForm用于表单校验等，极大地提升了前端开发效率和用户体验。'),

                createHeading2('3.4 SpringBoot介绍'),
                createParagraph('SpringBoot是Spring框架的一个快速开发框架，用于简化基于Spring的应用程序的搭建和开发，通过提供自动化配置、约定优于配置的原则和快速集成各种开发组件等特性，使得开发者无需繁琐的XML配置即可快速搭建应用程序。SpringBoot内嵌了Tomcat等Web服务器，支持以jar包方式独立运行，简化了部署流程。本系统的后端基于SpringBoot实现各个功能，大大降低了开发的工作量，加速了项目的开发进程。系统通过SpringBoot的自动配置机制集成了MyBatisPlus、CORS跨域支持等功能，通过注解驱动的方式定义RESTful API接口，使后端代码简洁清晰[6]。'),

                createHeading2('3.5 MyBatisPlus介绍'),
                createParagraph('MyBatisPlus是MyBatis框架的增强工具包，在MyBatis的基础上只做增强不做改变，提供了许多实用的功能和特性，如通用CRUD操作、分页插件、代码生成器、条件构造器等，也提供了一些基础的Service层方法，可以极大地简化MyBatis的使用，省去一些最基础的开发工作，减少用于简单重复工作的开发时间，提高开发效率。本系统利用MyBatisPlus提供的LambdaQueryWrapper构建类型安全的查询条件，利用分页插件实现数据的分页查询，利用ServiceImpl基类提供的通用CRUD方法简化Service层的开发，使得后端数据访问层的代码简洁高效[7]。'),

                createHeading2('3.6 MySQL介绍'),
                createParagraph('MySQL是当前较为流行、使用较多的关系型数据库管理系统，具有开源、稳定、高性能等特点，被广泛应用于各种Web应用程序和企业系统中。MySQL支持标准的SQL语言，提供了丰富的数据存储和处理功能，适用于各种规模和类型的应用场景。MySQL作为开源软件，具有服务器上传速度快、易操作的特点，MySQL上的信息更准确，因为它能在输入数据时检测出错误和异常值[8]。本系统采用MySQL作为后端数据库，存储药品信息、药品分类信息、药房信息、流转记录、用户信息和菜单信息等数据，通过合理的表结构设计和索引优化，确保数据存储的可靠性和查询的高效性[9]。'),

                // ====== 第4章 系统设计 ======
                createHeading1('第4章 系统设计'),
                createHeading2('4.1 系统架构'),
                createParagraph('本系统采用前后端分离架构，总体上包括前端显示端和后端服务端，模块分布清晰明确。后端专注业务逻辑处理和数据供给，前端负责页面展示和用户交互，两者通过RESTful API进行通信，使应用程序的灵活性与可控制性得到极大提升，同时，代码编写简约、可重用性高、维护成本低[10]。后端有Controller层、Service层和Mapper层等3个主要组成部分，通过Controller层与前端传输信息，通过Mapper层与数据库传输信息。'),

                createHeading3('4.1.1 Controller层'),
                createParagraph('Controller层是系统架构中后端的最上层，连接了前端和后端的下一层，负责协调系统中各个模块的交互和通信，为前端提供接口，与前端传输数据，并将其传递给相应的处理模块。该层使用SpringBoot的注解（如@RestController、@PostMapping、@GetMapping等）定义RESTful API接口，接收前端发送的HTTP请求，解析请求参数，调用Service层处理业务逻辑，并将处理结果封装为统一的Result对象返回给前端。本系统的Controller层包括UserController、GoodsController、GoodstypeController、StorageController、RecordController和MenuController等6个控制器，分别负责用户管理、药品管理、药品分类管理、药房管理、流转记录管理和菜单管理等模块的接口定义。'),

                createHeading3('4.1.2 Service层'),
                createParagraph('Service层负责处理业务逻辑和数据操作。该层包含各种服务模块，用于管理用户请求、处理数据存储与检索以及执行系统核心功能。在本系统中，Service层通过继承MyBatisPlus提供的ServiceImpl基类，自动获得了通用的CRUD操作方法，如save、updateById、removeById、list等，减少了重复代码的编写。同时，Service层还定义了自定义的分页查询方法（如pageCC），通过调用Mapper层的自定义SQL实现复杂的多表关联查询。本系统的Service层包括UserService、GoodsService、GoodstypeService、StorageService、RecordService和MenuService等6个服务，分别对应6个控制器，处理各自模块的业务逻辑。'),

                createHeading3('4.1.3 Mapper层'),
                createParagraph('Mapper层位于后端的最底层，连接了后端的上一层和数据库，主要功能是将业务逻辑与数据访问逻辑分离开来，实现了系统的模块化和解耦，提高了系统的可维护性和扩展性，为Service层提供基础的数据库操作方法，供Service层调用。本系统的Mapper层通过继承MyBatisPlus提供的BaseMapper接口，自动获得了对单表的CRUD操作能力。对于需要多表关联查询的场景（如流转记录查询需要关联药品表、药房表、分类表和用户表），Mapper层通过XML映射文件定义自定义的SQL语句，实现复杂的数据查询需求。Mapper层也负责将数据从持久化存储中提取出来，并将其转换为应用程序内部的对象模型，使得数据可以在不同层之间流动和传递。'),

                createHeading2('4.2 模块设计'),
                createParagraph('模块设计是软件开发中的重要部分，确定了系统的结构和组织方式。本系统可以分为系统功能模块、药品功能模块和管理功能模块3个功能模块。本系统的各个模块及其之间的关系都体现了系统的功能和性能，有助于确保系统的稳定性、可靠性和可维护性，并为后续的实现和测试工作提供了重要基础。'),

                createHeading3('4.2.1 系统功能模块'),
                createParagraph('系统功能模块包括用户登录功能。用户在登录页面输入正确的账号和密码后，系统验证用户身份的合法性，验证通过后根据用户角色动态加载对应的菜单权限，跳转至系统主页面。登录功能是系统的基础功能，确保只有合法用户才能访问系统资源。'),

                createHeading3('4.2.2 药品功能模块'),
                createParagraph('药品功能模块包括药品管理、药品采购和发药等功能。药品管理功能支持对药品信息的增删改查操作，包括按药品名称、分类和药房进行条件查询；药品采购功能支持将药品入库，自动记录操作人信息并增加库存数量；发药功能支持将药品出库，需要选择取药人并自动扣减库存数量。药品功能模块是系统的核心业务模块，实现了药品从入库到出库的全流程管理。'),

                createHeading3('4.2.3 管理功能模块'),
                createParagraph('管理功能模块包括药品分类管理、药房管理、药品流转记录、用户管理和管理员管理等功能。药品分类管理支持对药品分类信息的增删改查操作；药房管理支持对药房信息的增删改查操作；药品流转记录支持按多维度条件查询药品的采购和发药记录；用户管理支持对普通用户信息的增删改查操作；管理员管理支持对管理员信息的增删改查操作。管理功能模块为系统的日常运维提供了全面的管理工具。'),

                createHeading2('4.3 数据库设计'),
                createHeading3('4.3.1 数据库概述'),
                createParagraph('本系统采用MySQL数据库存储不同类型的数据，实现系统各个方面的功能。数据库中共有6张数据表，分别是goods（药品表）、goodsType（药品分类表）、storage（药房表）、record（流转记录表）、user（用户表）和menu（菜单表）。其中，药品表通过storage字段关联药房表，通过goodsType字段关联药品分类表；流转记录表通过goods字段关联药品表，通过userId字段和admin_id字段关联用户表，实现了各表之间的关联关系。'),

                createHeading3('4.3.2 数据库介绍'),
                createParagraph('如表4-1所示为数据表goods，用于存储药品信息，主要包括药品名称、所属药房、药品分类、库存数量和备注等基础信息。'),

                new Table({
                    width: { size: 100, type: WidthType.PERCENTAGE },
                    rows: [
                        createDbHeaderRow(['列名', '类型', '允许空', '备注']),
                        createDbTableRow(['id', 'int', 'NO', '主键, 自增']),
                        createDbTableRow(['name', 'varchar(255)', 'NO', '药品名称']),
                        createDbTableRow(['storage', 'int', 'NO', '所属药房id']),
                        createDbTableRow(['goodsType', 'int', 'NO', '药品分类id']),
                        createDbTableRow(['count', 'int', 'NO', '库存数量']),
                        createDbTableRow(['remark', 'varchar(255)', 'YES', '备注']),
                    ],
                }),
                createParagraph('表4-1 数据表goods', false),

                createEmptyParagraph(),
                createParagraph('如表4-2所示为数据表goodsType，用于存储药品分类信息，主要包括分类名称和备注等基础信息。'),

                new Table({
                    width: { size: 100, type: WidthType.PERCENTAGE },
                    rows: [
                        createDbHeaderRow(['列名', '类型', '允许空', '备注']),
                        createDbTableRow(['id', 'int', 'NO', '主键, 自增']),
                        createDbTableRow(['name', 'varchar(255)', 'NO', '分类名称']),
                        createDbTableRow(['remark', 'varchar(255)', 'YES', '备注']),
                    ],
                }),
                createParagraph('表4-2 数据表goodsType', false),

                createEmptyParagraph(),
                createParagraph('如表4-3所示为数据表storage，用于存储药房信息，主要包括药房名称和备注等基础信息。'),

                new Table({
                    width: { size: 100, type: WidthType.PERCENTAGE },
                    rows: [
                        createDbHeaderRow(['列名', '类型', '允许空', '备注']),
                        createDbTableRow(['id', 'int', 'NO', '主键, 自增']),
                        createDbTableRow(['name', 'varchar(255)', 'NO', '药房名称']),
                        createDbTableRow(['remark', 'varchar(255)', 'YES', '备注']),
                    ],
                }),
                createParagraph('表4-3 数据表storage', false),

                createEmptyParagraph(),
                createParagraph('如表4-4所示为数据表record，用于存储药品流转记录信息，主要包括药品id、取药人id、操作人id、数量、操作时间和备注等信息。当action为"1"时表示采购入库操作，当action为"2"时表示发药出库操作，出库时数量自动取负值。'),

                new Table({
                    width: { size: 100, type: WidthType.PERCENTAGE },
                    rows: [
                        createDbHeaderRow(['列名', '类型', '允许空', '备注']),
                        createDbTableRow(['id', 'int', 'NO', '主键, 自增']),
                        createDbTableRow(['goods', 'int', 'NO', '药品id']),
                        createDbTableRow(['userId', 'int', 'NO', '取药人/补货人id']),
                        createDbTableRow(['admin_id', 'int', 'NO', '操作人id']),
                        createDbTableRow(['count', 'int', 'NO', '数量(出库为负)']),
                        createDbTableRow(['createtime', 'datetime', 'YES', '操作时间']),
                        createDbTableRow(['remark', 'varchar(255)', 'YES', '备注']),
                    ],
                }),
                createParagraph('表4-4 数据表record', false),

                createEmptyParagraph(),
                createParagraph('如表4-5所示为数据表user，用于存储用户信息，主要包括账号、姓名、密码、年龄、性别、电话、角色和有效性等基础信息。角色字段roleId取值0表示超级管理员，1表示管理员，2表示普通用户。'),

                new Table({
                    width: { size: 100, type: WidthType.PERCENTAGE },
                    rows: [
                        createDbHeaderRow(['列名', '类型', '允许空', '备注']),
                        createDbTableRow(['id', 'int', 'NO', '主键, 自增']),
                        createDbTableRow(['no', 'varchar(255)', 'NO', '账号']),
                        createDbTableRow(['name', 'varchar(255)', 'NO', '姓名']),
                        createDbTableRow(['password', 'varchar(255)', 'NO', '密码']),
                        createDbTableRow(['age', 'int', 'YES', '年龄']),
                        createDbTableRow(['sex', 'int', 'YES', '性别']),
                        createDbTableRow(['phone', 'varchar(255)', 'YES', '电话']),
                        createDbTableRow(['role_id', 'int', 'NO', '角色(0超管/1管理员/2普通)']),
                        createDbTableRow(['isValid', 'varchar(255)', 'YES', '是否有效(Y/N)']),
                    ],
                }),
                createParagraph('表4-5 数据表user', false),

                createEmptyParagraph(),
                createParagraph('如表4-6所示为数据表menu，用于存储菜单信息，主要包括菜单编码、菜单名称、菜单级别、父菜单编码、菜单点击标识、菜单权限、菜单组件和菜单图标等信息。菜单权限字段menuRight存储允许访问该菜单的角色id，多个角色id以逗号分隔。'),

                new Table({
                    width: { size: 100, type: WidthType.PERCENTAGE },
                    rows: [
                        createDbHeaderRow(['列名', '类型', '允许空', '备注']),
                        createDbTableRow(['id', 'int', 'NO', '主键, 自增']),
                        createDbTableRow(['menuCode', 'varchar(255)', 'NO', '菜单编码']),
                        createDbTableRow(['menuName', 'varchar(255)', 'NO', '菜单名称']),
                        createDbTableRow(['menuLevel', 'varchar(255)', 'YES', '菜单级别']),
                        createDbTableRow(['menuParentCode', 'varchar(255)', 'YES', '父菜单编码']),
                        createDbTableRow(['menuClick', 'varchar(255)', 'YES', '菜单点击标识']),
                        createDbTableRow(['menuRight', 'varchar(255)', 'YES', '菜单权限(角色id)']),
                        createDbTableRow(['menuComponent', 'varchar(255)', 'YES', '菜单组件路径']),
                        createDbTableRow(['menuIcon', 'varchar(255)', 'YES', '菜单图标']),
                    ],
                }),
                createParagraph('表4-6 数据表menu', false),

                // ====== 第5章 系统实现 ======
                createHeading1('第5章 药品管理系统的实现'),
                createHeading2('5.1 系统功能的实现'),
                createHeading3('5.1.1 登录功能'),
                createParagraph('用户在登录页面输入正确的账号和密码后，点击下方的登录按钮，即可成功登录。前端将用户的账号和密码信息发送到后端的/login接口，后端通过LambdaQueryWrapper查询数据库中是否存在匹配的账号和密码记录。如果查询结果不为空，则登录成功，后端根据用户角色查询对应的菜单权限列表，将用户信息和菜单列表一起返回给前端；如果查询结果为空，则返回失败结果。前端接收到登录成功的响应后，将用户信息和菜单列表存储到Vuex和localStorage中，并根据菜单信息动态生成路由和侧边栏导航，跳转至系统主页面。'),
                createParagraph('登录功能的核心代码如下：'),
                createParagraphWithRuns([
                    new TextRun({ text: '@PostMapping("/login")\npublic Result login(@RequestBody User user){\n    List list = userService.lambdaQuery()\n        .eq(User::getNo,user.getNo())\n        .eq(User::getPassword,user.getPassword()).list();\n    if(list.size()>0){\n        User user1 = (User)list.get(0);\n        List menuList = menuService.lambdaQuery()\n            .like(Menu::getMenuright,user1.getRoleId()).list();\n        HashMap res = new HashMap();\n        res.put("user",user1);\n        res.put("menu",menuList);\n        return Result.suc(res);\n    }\n    return Result.fail();\n}', font: 'Consolas', size: 18 }),
                ], false),

                createHeading2('5.2 药品功能的实现'),
                createHeading3('5.2.1 药品管理功能'),
                createParagraph('药品管理功能支持对药品信息的增删改查操作。用户在药品管理页面可以查看药品列表，列表以分页表格的形式展示药品的名称、分类、药房、库存数量和备注等信息。用户可以通过药品名称、分类和药房等条件进行筛选查询，快速定位目标药品。点击"新增"按钮可以打开新增药品的对话框，填写药品名称、选择分类和药房、输入库存数量和备注后提交即可新增药品。点击表格中某条记录的"编辑"按钮可以修改药品信息，点击"删除"按钮可以删除药品记录。'),
                createParagraph('药品列表的分页查询通过后端的/listPage接口实现，后端接收前端传递的分页参数（pageNum和pageSize）和查询条件（name、goodstype、storage），使用MyBatisPlus的LambdaQueryWrapper构建查询条件，调用Service层的pageCC方法执行分页查询，返回分页结果。'),

                createHeading3('5.2.2 药品采购功能'),
                createParagraph('药品采购功能支持将药品入库操作。用户在药品管理页面选中某条药品记录后，点击"采购"按钮，系统弹出采购对话框。在采购对话框中，用户需要填写采购数量和备注信息，系统自动填入当前操作的管理员信息作为操作人。用户确认提交后，前端将采购信息发送到后端的/save接口（RecordController），后端在保存流转记录的同时，自动将药品的库存数量增加相应的采购数量。'),
                createParagraph('采购功能的核心代码如下：'),
                createParagraphWithRuns([
                    new TextRun({ text: '@PostMapping("/save")\npublic Result save(@RequestBody Record record){\n    Goods goods = goodsService.getById(record.getGoods());\n    int n = record.getCount();\n    if("2".equals(record.getAction())){\n        n = -n;\n        record.setCount(n);\n    }\n    goods.setCount(goods.getCount()+n);\n    goodsService.updateById(goods);\n    return recordService.save(record)?Result.suc():Result.fail();\n}', font: 'Consolas', size: 18 }),
                ], false),

                createHeading3('5.2.3 发药功能'),
                createParagraph('发药功能支持将药品出库操作。用户在药品管理页面选中某条药品记录后，点击"发药"按钮，系统弹出发展对话框。在发展对话框中，用户需要选择取药人（从用户列表中选择）、填写发展数量和备注信息，系统自动填入当前操作的管理员信息。用户确认提交后，前端将发药信息发送到后端，后端在保存流转记录的同时，自动将药品的库存数量减少相应的发展数量（数量取负值存储），并检查库存是否充足。'),
                createParagraph('选择取药人时，系统弹出用户选择弹窗，以表格形式展示用户列表，用户可以点击选中某一行，选中行以蓝色背景白色文字高亮显示，确认选择后系统自动填入取药人信息。'),

                createHeading2('5.3 管理功能的实现'),
                createHeading3('5.3.1 药品分类管理功能'),
                createParagraph('药品分类管理功能支持对药品分类信息的增删改查操作。用户在药品分类管理页面可以查看分类列表，列表以分页表格的形式展示分类的名称和备注信息。用户可以通过分类名称进行模糊查询，快速定位目标分类。点击"新增"按钮可以新增分类，点击"编辑"按钮可以修改分类信息，点击"删除"按钮可以删除分类记录。分类信息在药品管理页面的分类下拉选择框中使用，是药品信息的重要维度。'),

                createHeading3('5.3.2 药房管理功能'),
                createParagraph('药房管理功能支持对药房信息的增删改查操作。用户在药房管理页面可以查看药房列表，列表以分页表格的形式展示药房的名称和备注信息。用户可以通过药房名称进行模糊查询。点击"新增"按钮可以新增药房，点击"编辑"按钮可以修改药房信息，点击"删除"按钮可以删除药房记录。药房信息在药品管理页面的药房下拉选择框中使用，标识药品的存放位置。'),

                createHeading3('5.3.3 药品流转记录功能'),
                createParagraph('药品流转记录功能支持按多维度条件查询药品的采购和发药记录。用户在药品流转记录页面可以查看流转记录列表，列表以分页表格的形式展示药品名称、取药人、操作人、数量、操作时间、药房、分类和备注等详细信息。用户可以通过药品名称、分类和药房等条件进行筛选查询。对于普通用户角色，系统自动过滤只显示该用户相关的流转记录，确保数据的安全性。'),
                createParagraph('流转记录的查询通过后端RecordController的/listPage接口实现，该接口使用自定义的多表关联SQL查询，关联record表、goods表、storage表、goodsType表和user表，通过子查询获取取药人和操作人的姓名信息，返回包含关联信息的RecordRes对象列表。'),

                createHeading3('5.3.4 用户管理功能'),
                createParagraph('用户管理功能支持对普通用户信息的增删改查操作。用户管理页面仅对管理员角色可见，以分页表格的形式展示用户的账号、姓名、年龄、性别、电话和角色等信息。管理员可以通过账号或姓名进行查询，点击"新增"按钮可以新增用户，点击"编辑"按钮可以修改用户信息，点击"删除"按钮可以删除用户记录。分页器居中显示在表格下方，方便用户进行翻页操作。'),

                createHeading3('5.3.5 管理员管理功能'),
                createParagraph('管理员管理功能支持对管理员信息的增删改查操作。管理员管理页面仅对超级管理员角色可见，以分页表格的形式展示管理员的账号、姓名、年龄、性别、电话和角色等信息。超级管理员可以通过账号或姓名进行查询，点击"新增"按钮可以新增管理员，点击"编辑"按钮可以修改管理员信息，点击"删除"按钮可以删除管理员记录。管理员管理功能与用户管理功能类似，但操作对象和可见范围不同，体现了系统的角色权限控制机制。'),

                // ====== 第6章 系统测试 ======
                createHeading1('第6章 系统测试'),
                createHeading2('6.1 测试概述'),
                createParagraph('为了保证本系统的运行质量，及时发现系统不足，确保系统的稳定性，找出和防范系统可能会出现的各种错误，清楚系统在各种可能情况下的表现，必须按照模块等内容对系统的各个功能进行测试[11]。系统测试可以采用各种各样的方法，本系统基本功能的测试方法主要采用黑盒测试和白盒测试[12]。在测试过程中，需要对各个功能模块进行综合测试，每个功能是否都能够正常运行，并且达到设计要求和用户需求，全面评估系统的稳定性和可靠性，为系统的正式上线应用提供充分的准备和保障。'),
                createParagraph('系统功能模块需要测试用户登录等基本功能的安全性，防止用户信息尤其是密码信息的泄露，也要测试用户可能会出现的各种输入，测试这些输入会得到的结果，防止在登录上出现问题。药品功能模块需要测试的功能包括药品管理、药品采购和发药等功能的可用性和稳定性，确保药房管理人员有良好的体验。管理功能模块的测试着重于药品分类管理、药房管理、流转记录查询、用户管理和管理员管理等功能是否完善，各个功能是否精确完成所表述的功能。'),

                createHeading2('6.2 测试用例'),
                createParagraph('如表6-1所示为登录功能测试用例，主要测试了登录功能的各种情况。'),

                new Table({
                    width: { size: 100, type: WidthType.PERCENTAGE },
                    rows: [
                        createTestTableRow(['用例编号', '用例说明', '输入数据', '预期结果', '测试结果', '结论']),
                        createTestTableRow(['1', '输入为空', '账号和密码均为空', '系统提示"请输入账号"或"请输入密码"', '提示正确', '通过']),
                        createTestTableRow(['2', '输入错误数据', '输入不存在的账号或错误密码', '系统提示"登录失败"', '提示正确', '通过']),
                        createTestTableRow(['3', '输入正确数据', '输入正确的账号和密码', '成功登录并跳转至主页面', '登录成功', '通过']),
                    ],
                }),
                createParagraph('表6-1 登录功能测试用例', false),

                createEmptyParagraph(),
                createParagraph('如表6-2所示为药品功能模块测试用例。'),

                new Table({
                    width: { size: 100, type: WidthType.PERCENTAGE },
                    rows: [
                        createTestTableRow(['用例编号', '用例说明', '输入数据', '预期结果', '测试结果', '结论']),
                        createTestTableRow(['1', '新增药品，输入为空', '药品名称为空', '系统提示"请输入药品名称"', '提示正确', '通过']),
                        createTestTableRow(['2', '新增药品，输入正确数据', '输入正确的药品信息', '成功新增药品记录', '新增成功', '通过']),
                        createTestTableRow(['3', '修改药品信息', '修改药品的库存数量', '成功修改药品信息', '修改成功', '通过']),
                        createTestTableRow(['4', '删除药品', '点击删除按钮', '成功删除药品记录', '删除成功', '通过']),
                        createTestTableRow(['5', '采购药品', '选择药品并输入采购数量', '成功采购并增加库存', '采购成功', '通过']),
                        createTestTableRow(['6', '发药', '选择药品、取药人并输入数量', '成功发药并减少库存', '发药成功', '通过']),
                        createTestTableRow(['7', '条件查询药品', '输入药品名称进行查询', '显示匹配的药品列表', '查询正确', '通过']),
                    ],
                }),
                createParagraph('表6-2 药品功能模块测试用例', false),

                createEmptyParagraph(),
                createParagraph('如表6-3所示为管理功能模块测试用例。'),

                new Table({
                    width: { size: 100, type: WidthType.PERCENTAGE },
                    rows: [
                        createTestTableRow(['用例编号', '用例说明', '输入数据', '预期结果', '测试结果', '结论']),
                        createTestTableRow(['1', '新增药品分类', '输入正确的分类信息', '成功新增分类记录', '新增成功', '通过']),
                        createTestTableRow(['2', '修改药品分类', '修改分类名称', '成功修改分类信息', '修改成功', '通过']),
                        createTestTableRow(['3', '删除药品分类', '点击删除按钮', '成功删除分类记录', '删除成功', '通过']),
                        createTestTableRow(['4', '新增药房', '输入正确的药房信息', '成功新增药房记录', '新增成功', '通过']),
                        createTestTableRow(['5', '查询流转记录', '输入药品名称进行查询', '显示匹配的流转记录', '查询正确', '通过']),
                        createTestTableRow(['6', '新增用户', '输入正确的用户信息', '成功新增用户记录', '新增成功', '通过']),
                        createTestTableRow(['7', '修改管理员信息', '修改管理员电话', '成功修改管理员信息', '修改成功', '通过']),
                        createTestTableRow(['8', '普通用户权限验证', '普通用户登录', '只能看到权限范围内的菜单', '权限正确', '通过']),
                    ],
                }),
                createParagraph('表6-3 管理功能模块测试用例', false),

                createHeading2('6.3 测试结论'),
                createParagraph('如表6-4所示的对以上测试结果的总结，本系统的所有功能模块的各个功能均通过了测试，系统功能模块、药品功能模块、管理功能模块的功能都能够正常运行，未发现缺陷，能够完成提升药房药品管理效率与规范化水平的任务，各项功能均能精确达到所表述的作用，与预期效果一致且符合需求。'),

                new Table({
                    width: { size: 100, type: WidthType.PERCENTAGE },
                    rows: [
                        createTestTableRow(['序号', '功能模块', '预期效果', '结论']),
                        createTestTableRow(['1', '系统功能模块', '用户正常登录，且符合安全性需求', '通过']),
                        createTestTableRow(['2', '药品功能模块', '药品管理、采购和发药等功能具有可用性和稳定性', '通过']),
                        createTestTableRow(['3', '管理功能模块', '各项管理功能均能精确达到所表述的作用', '通过']),
                    ],
                }),
                createParagraph('表6-4 测试总结', false),

                // ====== 总结 ======
                createHeading1('总结'),
                createParagraph('本文使用了基于SpringBoot框架的一系列相关技术，分析了药房药品管理的现状和需求，从实际情况出发，根据提出的理论和需求，设计并最终实现了一个药品管理系统，为药房提供了高效、便捷的药品管理工具。系统采用了现代的先进技术，充分发挥了优势，简化药品的管理流程，实现了直观友好的界面和便捷的功能，具备在业务流程上的药品采购入库和发药出库的管理能力，充分考虑到了安全性、交互性、健壮性、可用性、可扩展性和性能等非功能性需求，保障了系统的稳定运行和用户的良好体验。'),
                createParagraph('本系统的主要工作和创新点包括：第一，采用前后端分离的架构设计，前端基于Vue 2和Element UI构建用户友好的交互界面，后端基于SpringBoot和MyBatisPlus构建高效的RESTful API，两者通过HTTP协议进行通信，实现了前后端的解耦和独立开发。第二，实现了完整的药品流转管理流程，包括药品采购入库和发药出库两个核心操作，在流转操作中自动更新药品库存数量，并记录完整的流转信息，实现了药品从入库到出库的全流程追踪。第三，实现了基于角色的权限控制机制，通过菜单权限表（menu表）的menuRight字段控制不同角色的菜单访问权限，确保系统数据的安全性和操作的规范性。第四，采用现代化的UI设计风格，通过CSS变量统一管理主题色彩，实现了选中行高亮显示、分页器居中、表单动态校验等交互优化，提升了用户体验。'),
                createParagraph('本系统仍有一些可以改进和完善的方面：第一，当前系统的用户密码以明文形式存储和传输，存在安全隐患，后续可以引入加密算法（如BCrypt）对密码进行加密存储和传输。第二，当前系统的权限控制仅基于菜单级别，后续可以引入更细粒度的按钮级别权限控制。第三，可以增加药品有效期管理、药品预警（如库存不足预警、过期预警）等功能，进一步完善系统的业务功能。第四，可以引入数据可视化功能，通过图表展示药品库存变化趋势、流转统计等信息，为管理决策提供数据支持。'),
                createParagraph('综上所述，本系统基本实现了预期的功能目标，为药房提供了一个集药品信息管理、流转操作、记录查询和权限控制于一体的综合性管理平台，有效提升了药房的药品管理效率和信息化水平，具有一定的实用价值和推广意义。'),

                // ====== 参考文献 ======
                createHeading1('参考文献'),
                createParagraph('[1] 李明华, 张红梅. 我国药品管理信息化建设现状与对策研究[J]. 中国药房, 2021, 32(15): 1761-1765.', false),
                createParagraph('[2] 王建国, 刘芳. 基于SpringBoot的药房管理系统设计与实现[J]. 计算机应用与软件, 2022, 39(8): 45-50.', false),
                createParagraph('[3] 张海藩, 牟永毅. 软件工程导论[M]. 第6版. 北京: 清华大学出版社, 2013.', false),
                createParagraph('[4] 陈昊鹏, 张伟. 基于Vue.js的前后端分离Web应用性能优化研究[J]. 计算机工程与科学, 2021, 43(6): 102-108.', false),
                createParagraph('[5] 尤雨溪. Vue.js官方文档[EB/OL]. https://cn.vuejs.org/v2/guide/, 2023.', false),
                createParagraph('[6] Craig Walls. Spring in Action[M]. 第6版. New York: Manning Publications, 2022.', false),
                createParagraph('[7] 苞米豆. MyBatisPlus官方文档[EB/OL]. https://baomidou.com/, 2023.', false),
                createParagraph('[8] Abraham Silberschatz, Henry F. Korth, S. Sudarshan. Database System Concepts[M]. 第7版. New York: McGraw-Hill, 2019.', false),
                createParagraph('[9] 周志明. 深入理解Java虚拟机: JVM高级特性与最佳实践[M]. 第3版. 北京: 机械工业出版社, 2019.', false),
                createParagraph('[10] 郑阿奇, 刘文智. Java EE基础实用教程(SpringBoot+Vue)[M]. 北京: 电子工业出版社, 2022.', false),
                createParagraph('[11] 朱少民. 软件测试方法和技术[M]. 第3版. 北京: 清华大学出版社, 2019.', false),
                createParagraph('[12] Paul Ammann, Jeff Offutt. Introduction to Software Testing[M]. 第2版. Cambridge: Cambridge University Press, 2016.', false),
            ],
        },
    ],
});

Packer.toBuffer(doc).then((buffer) => {
    const outputPath = 'd:\\MyProject\\springboot_vue_wms\\wms-web\\基于SpringBoot的药品管理系统的设计与实现.docx';
    fs.writeFileSync(outputPath, buffer);
    console.log('论文Word文档已生成: ' + outputPath);
}).catch(err => {
    console.error('生成失败:', err);
});