<script>
// 图标兼容组件：支持旧 element-ui 类名（el-icon-*）与 Element Plus 组件名两种入参
// 用途：侧边栏 DB menuIcon、模板中三元/动态图标名
// 用 ElIcon 包裹保证与 <el-icon><HomeFilled/></el-icon> 尺寸一致（18px）
import { h } from 'vue'
import { ElIcon } from 'element-plus'
import * as Icons from '@element-plus/icons-vue'
import { legacyIconMap, FALLBACK_ICON } from '../utils/iconMap'

export default {
    name: 'FIcon',
    props: {
        name: { type: String, default: '' }
    },
    computed: {
        iconComp() {
            const n = (this.name || '').trim()
            if (Icons[n]) return Icons[n]
            const mapped = legacyIconMap[n]
            return (mapped && Icons[mapped]) || Icons[FALLBACK_ICON]
        }
    },
    render() {
        return h(ElIcon, { class: 'f-icon' }, () => h(this.iconComp))
    }
}
</script>

<style scoped>
/* 间距和尺寸由父组件 Aside 统一控制 */
</style>
