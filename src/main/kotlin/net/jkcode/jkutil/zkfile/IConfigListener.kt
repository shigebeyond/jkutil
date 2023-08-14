package net.jkcode.jkutil.zkfile

/**
 * 配置变化的监听器
 *
 * @author shijianhang
 * @create 2023-7-13 下午10:38
 **/
interface IConfigListener {

    /**
     * 处理配置数据变化
     * @param data 变化的配置数据，如果为null表示配置文件删除了
     */
     fun handleConfigChange(data: Map<String, Any?>?){}

}