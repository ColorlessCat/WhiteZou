package com.colorlesscat.whitezou.functions

/**
@Author ColorlessCat
@Date 2020-08-04 18:56
@Description 把所有的依赖都集中在这一层上，下层的代码基本不会修改，即使修改对整个逻辑也不会产生影响
 */
class NetFunctionImpl : NetFunction {

    override fun login(usr: String, pass: String): Boolean {
        val result = NetOperation.getLoginCookie(usr, pass)
        if (result != "failed") {
            ObjectSaver.cookie = result
            return true
        }
        return false


    }

    override fun getFileList(id: String): List<LanzousFile> {
        if (ObjectSaver.cookie == "") return emptyList()
        val list = mutableListOf<LanzousFile>()
        var pg = 0
        var data: String
        var tempList: MutableList<LanzousFile>
        var isFile = false
        /**
         * 这里的思路是先获取所有的文件夹再获取所有的文件。
         * 然而蓝奏云的接口，无论有多少个文件夹，获取的时候都是一次性返回全部，只有文件才会分页。
         * 原来的实现先留着，可能，以后会改？
         */
        while (true) {
            data = NetOperation.getFilesData(ObjectSaver.cookie, isFile, id, pg)
            tempList = DataOperation.parseJson2FileList(data)
//            if (tempList.isEmpty() && !isFile) {
//                isFile = true
//                pg=0
//                continue
//            } else if (tempList.isEmpty())
//                break
            if (tempList.isEmpty() && isFile) break
            list.addAll(tempList)
            isFile = true
            pg++
        }
        return list
    }

    override fun getFileData(id: String, isFile: Boolean): Array<String> =
        DataOperation.parseJson4FileData(NetOperation.getFileData(ObjectSaver.cookie, isFile, id))

    override fun deleteFile(id: String, isFile: Boolean): Boolean =
        NetOperation.deleteFile(ObjectSaver.cookie, isFile, id)[6] == '1'

    override fun addFolder(name: String, description: String, parentId: String) =
        DataOperation.parseJson4AddFolder(
            NetOperation.addFolder(
                ObjectSaver.cookie,
                name,
                description,
                parentId
            )
        )


    override fun getDownloadLink(url: String): String =
        NetOperation.getRealDownloadLink(
            DataOperation.parseDownloadLink(
                NetOperation.getFileDownloadLink(
                    url
                )
            )
        )

    override fun getFileSize(url: String)=
        NetOperation.getDownloadFileSize(getDownloadLink(url))

    override fun setDirData(id: String, name: String, des: String) =
        NetOperation.changeDirInfo(ObjectSaver.cookie, id, name, des)[6] == '1'

    override fun removeFile(id: String, dirID: String) =
        NetOperation.moveFile(ObjectSaver.cookie, id, dirID)[6] == '1'


}