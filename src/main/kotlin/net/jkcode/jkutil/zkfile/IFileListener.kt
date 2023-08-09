package net.jkcode.jkutil.zkfile

/**
 * 文件变化的监听器：监听某个配置文件的增删改变化
 * 有2个实现类
 *   1 ZkChildListener 适配器模式, 将zk监听器接口转为代理调用fileListener接口, 就是将zk节点/数据变化事件转为文件增删改事件, 好实现zk配置(文件)动态刷新
 *   2 ZkConfigFiles zk配置文件, 就是被ZkChildListener代理调用 来实现动态刷新配置
 * 
 * @author shijianhang
 * @create 2023-7-13 下午10:38
 **/
interface IFileListener {

    /**
     * 处理配置文件新增
     * @param path
     */
     fun handleFileAdd(path: String, content: String)

    /**
     * 处理配置文件删除
     * @param path
     */
     fun handleFileRemove(path: String)

    /**
     * 处理文件内容变化
     * @param path
     * @param content
     */
     fun handleContentChange(path: String, content: String)

}