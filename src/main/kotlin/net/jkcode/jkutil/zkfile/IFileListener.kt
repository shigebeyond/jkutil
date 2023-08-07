package net.jkcode.jkutil.zkfile

/**
 * 文件变化的监听器：监听某个服务文件变化
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