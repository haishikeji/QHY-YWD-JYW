<template>
    <div>
        <!-- flex盒子 -->
        <div class="bigBox" style="height: 38vh; overflow: auto;" v-if="fileList?.length">
            <template v-for="(item, index) in fileList" :key="index">
                <!-- 循环盒子 -->
                <div class="flexBox" @contextmenu="rightClick(item, $event)">
                    <!-- 图片 -->
                    <div class="imgBox">
                        <img :src="setImg(item.fileType)" alt="" style="vertical-align: middle;cursor: pointer;">
                    </div>
                    <!-- 名称 -->
                    <div class="nameBox">
                            <el-tooltip
                            class="box-item"
                            effect="dark"
                            placement="top"
                            :show-after="1000"
                            :content="item.fileName"
                        ><p class="nameName" style="cursor: pointer;">{{ item.fileName }}</p></el-tooltip>
                    </div>
                    <div class="stars">
                            <el-icon v-if="item.isFavorite == 'N'" @click.stop="starFolder(item,true)" style="margin-right: 7px;">
                            <Star />
                        </el-icon>
                        <img v-else @click.stop="delStarFolder(item,true)" src="@/assets/images/yellowstar.png" alt="">
                    </div>
                </div>
            </template>
            <div class="setCli" v-if="cliCC" :style="{ left: xz + 'px', top: (yz - 330) + 'px' }">
                <template v-for="(item, index) in filterMouseCli()" :key="index">
                    <p @click="chooseSet(item, index, 1)" class="chooseSet" style="cursor: pointer;">
                        <img :src="item.img" alt="">
                        {{ item.name }}
                        <span v-if="item.name == '协作' ? true : false"
                            style="color: #7084B4;float: right;position: relative;" class="arrow">></span>
                    </p>
                </template>
            </div>
            <div class="setCli1" v-if="anyP" :style="{ left: xz + 155 + 'px', top: yz - 50 + 'px' }">
                <p @click="chooseSet1(0)" class="chooseSet">
                    <img src="../../../assets/images/user.png" alt="" style="cursor: pointer;">
                    选择人员
                </p>
                <p class="chooseSet" @click="goLock">
                    <img src="../../../assets/images/archiveTray.png" alt="" style="cursor: pointer;">
                    归档
                </p>
            </div>
            <!-- <div>
                <TransferModal v-if="thanks" :thanks="thanks" :clickRowId="clickRowId" :workOrEdit="workOrEdit"
                    @getCback="getCback"></TransferModal>
            </div> -->
            <!-- 复制粘贴 -->
            <div>
                <FileTree v-if="fileTrees" :fileTrees="fileTrees" :newSpaceId="newSpaceId" :fileId="clickRowId"
                    @getChildren="getChildren" :copyOrMove="copyOrMove"></FileTree>
            </div>
            <div>
                <ImgPreview :previewData="previewData" :copyFileType="copyFileType" :showPreview="showPreview"
                    @closeImgPreview="closeImgPreview"></ImgPreview>
            </div>
            <div>
                <FileCollect v-if="collects" :collects="collects" @getCollects="getCollects" :copyFileName="copyFileName"
                    :copyFileId="copyFileId" :isFolder="isFolder" :clickRow="clickRow"></FileCollect>
            </div>
        </div>
        <div v-else  class="none-data">暂无数据</div>
    </div>
</template>

<script>
import { ref, onMounted, watch } from "vue"
import { ElMessage } from "element-plus";
import myfile from "../../../api/myfile/myfile";
import documents from "../../../api/document/document";
import fileCount from "../../../api/fileCount/fileCount";
import TransferModal from '../modalComponebts/Transfer.vue'
import ImgPreview from '@/components/ImgPreview/ImgPreview.vue'
import FileCollect from '../modalComponebts/FileCollect.vue'
import FileEdit from './FileEdit.vue'
import FileTree from './FileTree.vue'
import ImgFile from "../jsComponents/ImgFile";
import { canPreviewFile } from "@/utils/index.js"
import { preview } from "@/api/common/common.js";
import { listVersion } from "@/api/biz/version";
import { setIcon } from "@/utils/index.js";
export default {
    props: {
        getAllTop: {
            type: Function,
            required: true
        },
        thisFolder: {
            type: Object,
            required: true
        },
        fileList:{
            type:Array,
            require:true
        }
    },
    setup(props, { emit }) {
        let impDirId = ref(0)
        let impSpaceId = ref(0)
        let newSpaceId = ref(0)
        let newDirId = ref(0)
        let topPath = ref("")
        let fileListGrid = ref([])
        let folderList = ref([])
        let fileTotal = ref(0)
        let fileMenu = ref([])
        let isFolder = ref("")
        let clickRow = ref({})
        let copyRow = ref("")
        let clickRowId = ref("")
        let copyDirId = ref("")
        let copySpaceId = ref("")
        let copyFileId = ref("")
        let copyFileSize = ref("")
        let copyFileType = ref("")
        let workOrEdit = ref(0)
        let copyOrMove = ref(0)
        let nameForm = ref({
            name: ""
        })
        const historycopyRow = ref({})
        let copyFileName = ref("")
        let yz = ref(0)
        let xz = ref(0)
        let cliCC = ref(false)
        let anyP = ref(false)
        let thanks = ref(false)
        let fileTrees = ref(false)
        let onlyView = ref(false)
        let editOnline = ref(false)
        let showPreview = ref(false)
        let historyPrew = ref(false)
        let fileNameChange = ref(false)
        let collects = ref(false)
        let openForwardFile = ref(false)
        let historyTotal = ref(0)
        const fileUserTreeData = reactive({ data: {} });
        let previewData = ref()
        let mouseCli = ref([
            {
                img: ImgFile.previewIcon,
                name: "预览"
            },
            {
                img: ImgFile.addolder,
                name: "移动到...",
            },
            {
                img: ImgFile.copy,
                name: "复制到...",
            },
            {
                img: ImgFile.collect,
                name: "收藏"
            },
            {
                img: ImgFile.downLoad,
                name: "下载"
            },
            {
                img: ImgFile.textbox,
                name: "重命名"
            },
            {
                img: ImgFile.share,
                name: "分享"
            },
            {
                img: ImgFile.notePencil,
                name: "在线编辑"
            },
            {
                img: ImgFile.togger,
                name: "协作"
            },
            {
                img: ImgFile.icc,
                name: "文字识别"
            },
            {
                img: ImgFile.history,
                name: "历史版本"
            },
            {
                img: ImgFile.trash,
                name: "删除"
            }
        ])
        // 获取文件夹，中栏，文件
        function getAllTop() {
            documents.getTop(2).then(res => {
                let userMe = [res]
                impDirId.value = userMe[0].dirId//固定
                impSpaceId.value = userMe[0].spaceId//固定
                newSpaceId.value = userMe[0].spaceId//变化
                newDirId.value = userMe[0].dirId//变化
                topPath.value = userMe[0].dirPath
                let obj = {
                    pageNum: 1,
                    pageSize: 10
                }
                console.log('userMe===========', userMe)
                // documents.getALLdocument({ parentId: userMe[0].dirId - 0, spaceId: userMe[0].spaceId, pageNum: obj.pageNum, pageSize: obj.pageSize }).then(res => {
                //     fileMenu.value = res.data
                //     folderList.value = res.data
                // })
                const fileId =props.thisFolder.dirId?props.thisFolder.dirId:userMe[0].dirId - 0
                myfile.getById(fileId, obj).then(res => {
                    if (res.code === 200) {
                        fileListGrid.value = res.rows
                        fileTotal.value = res.total
                        if (fileTotal.value / 10 != 1) {
                            let x = Math.trunc(fileTotal.value / 10) + 1
                            for (var i = 1; i <= x; i++) {
                                obj.pageNum = i
                                myfile.getById(fileId, obj).then(res => {
                                    fileListGrid.value = fileListGrid.value.concat(res.rows)
                                    const uniquefileListGrid = Array.from(new Set(fileListGrid.value.map(item => item.docId)))
                                        .map(docId => fileListGrid.value.find(item => item.docId === docId));
                                    // 合并数组并更新 fileListGrid.value
                                    fileListGrid.value = uniquefileListGrid
                                    console.log(fileListGrid.value, 'iiii');
                                })
                            }
                        }
                    }
                })

            })
        }
        // 筛选后缀图片
        function getImage(file) {
            if (file === '.txt') {
                return ImgFile.bigTxt
            } else if (file === '.xlxs' || file === '.docx' || file === '.xls') {
                return ImgFile.bigxlsx
            } else if (file === '.pptx') {
                return ImgFile.bigpptx
            } else if (file === '.word') {
                return ImgFile.bigdoc
            } else if (file === '.pdf') {
                return ImgFile.bigpdf
            } else if (file === ".mp3") {
                return ImgFile.audio
            } else if (file === '.mp4') {
                return ImgFile.video
            } else if (file === ".png" || file === ".jpg") {
                return ImgFile.pic
            } else {
                return ImgFile.elseFile
            }
        }
        // function rightClick(row, event) {
        //     event.preventDefault()
        //     isFolder.value = "N"
        //     clickRow.value = row
        //     xz.value = event.pageX
        //     yz.value = event.pageY
        //     copyRow.value = row
        //     clickRowId.value = row.docId
        //     copyDirId.value = row.dirId
        //     copySpaceId.value = row.spaceId
        //     copyFileId.value = row.fileId
        //     copyFileSize.value = row.fileSize
        //     copyFileType.value = row.fileType
        //     nameForm.value.name = row.fileName
        //     copyFileName.value = row.fileName
        //     if (cliCC.value === true) {
        //         cliCC.value = false
        //     } else {
        //         cliCC.value = true
        //     }
        // }
       const rightClick = (row,e)=>{
        emit("handleRowClick", row,null,e)
       }
        //对mouseCli数组进行筛选，实现菜单的区分显示
        const filterMouseCli = () => {
            const canPreviewArray = ['.doc', '.docm', '.docx', '.dot', '.dotm', '.dotx', '.epub', '.fodt', '.htm', '.html', '.mht', '.odt', '.ott', '.pdf', '.rtf', '.txt', '.djvu', '.xps', 'csv', 'fods', 'ods', 'ots', 'xls', 'xlsm', 'xlsx', 'xlt', 'xltm', 'xltx', 'fodp', 'odp', 'otp', 'pot', 'potm', 'potx', 'pps', 'ppsm', 'ppsx', 'ppt', 'pptm', 'pptx']
            const typeArr = ['.png', '.jpg', '.jpeg', '.JPG', '.mp3', '.mp4']
            const imgTypeArr = ['.png', '.jpg', '.jpeg', '.JPG']
            const canEditArr = ['.doc', '.docm', '.docx', '.dot', '.dotm', '.dotx', '.txt', '.djvu', '.xps', 'csv', 'fods', 'ods', 'ots', 'xls', 'xlsm', 'xlsx', 'xlt', 'xltm', 'xltx', 'fodp', 'odp', 'otp']
            let arr = []
            if (!(typeArr.includes(copyFileType.value) || canPreviewArray.includes(copyFileType.value))) {
                arr = mouseCli.value.filter(item => item.name !== "预览")
            } else {
                arr = toRaw(mouseCli.value)
            }
            if (!canEditArr.includes(copyFileType.value)) {
                arr = arr.filter(item => item.name !== "在线编辑" && item.name !== "协作" && item.name !== "历史版本")
            }
            if (!imgTypeArr.includes(copyFileType.value)) {
                arr = arr.filter(item => item.name !== "文字识别")
            }
            return arr
        }
        function mouseClick() {
            if (cliCC.value) {
                cliCC.value = false
            }
            if (anyP) {
                anyP.value = false
            }
        }
        async function chooseSet(row, index, num) {

            if (row.name == '协作') {
                cliCC.value = true
            } else {
                cliCC.value = false
            }
            if (row.name == '分享') {
                workOrEdit.value = num
                thanks.value = true
                emit("shareBack", thanks.value, workOrEdit.value)
            } else {
                thanks.value = false
            }
            if (row.name == '协作') {
                if (anyP.value) {
                    anyP.value = false
                } else {
                    anyP.value = true
                }
            } else {
                anyP.value = false
            }
            console.log(anyP.value, 'anyp');
            if (row.name == '删除') {
                myfile.delMenu(clickRowId.value).then(res => {
                    if (res.code === 200) {
                        ElMessage({
                            type: "success",
                            message: "删除成功"
                        })
                        getAllTop()
                    }
                })
            }
            if (row.name === '复制到...') {
                copyOrMove.value = 0
                fileTrees.value = true
            }
            if (row.name === '移动到...') {
                copyOrMove.value = 1
                fileTrees.value = true
            }
            if (row.name === '重命名') {
                fileNameChange.value = true
                emit("navBack", fileNameChange.value, nameForm.value.name)
            }
            if (row.name === "下载") {
                location.href = `${import.meta.env.VITE_APP_BASE_API}/api/download/${copyFileId.value}`;
            }
            if (row.name === '收藏') {
                collects.value = true
            }
            if (row.name === '在线编辑') {
                editOnline.value = false
                emit("onlineCase", editOnline.value, clickRowId.value, copyRow.value)
            }
            if (row.name === '文字识别') {
                router.push({
                    path: '/identifyFont',
                    query: {
                        fileId: copyFileId.value,
                        fileType: copyFileType.value
                    }
                })
            }
            if (row.name === '预览') {
                // editOnline.value = true
                // emit("showFile", false)
                const filePreview = canPreviewFile(copyFileType.value)
                if (filePreview) {
                    onlyView.value = true
                    emit("showFile", false, clickRowId.value, copyRow.value)
                    cliCC.value = false
                } else {
                    showPreview.value = true
                    const res = await preview(copyFileId.value)
                    previewData.value = URL.createObjectURL(res)
                }
                showPreview.value = true
                const res = await preview(copyFileId.value)
                previewData.value = URL.createObjectURL(res)
            } else if (row.name === "历史版本") {
                const resHistory = await listVersion({ docId: clickRowId.value })
                historyTotal.value = resHistory.total
                fileUserTreeData.data = resHistory.rows;
                openForwardFile.value = true
                emit("lastBB", openForwardFile.value, historyTotal.value, fileUserTreeData.data)
            }
        }
        function changeMsgClose(val, item) {
            if (val) {
                historycopyRow.value = item
                editOnline.value = false
                cliCC.value = false
                historyPrew.value = true
            }
            openForwardFile.value = val
        }

        function chooseSet1(num) {
            workOrEdit.value = num
            thanks.value = true
            anyP.value = false
            emit("shareBack", thanks.value, workOrEdit.value)
        }
        function getChildren(data) {
            fileTrees.value = data
        }
        function getCback(data) {
            thanks.value = data
        }
        function closeImgPreview(data) {
            showPreview.value = data
        }
        function cancleHistoryPrew(data) {
            historyPrew.value = false
        }
        function getCollects(data) {
            collects.value = false
        }
        const setImg = (type)=>{
           return setIcon(type)
        }
        const starFolder = (item) => {
            emit("collectByStar", item,false)
        }
        const delStarFolder = (item) => {
            emit("delCollect", item,false)
        }
        // 归档
        function goLock() {
            fileCount.lockActor(clickRowId.value).then(res => {
                if (res.code === 200) {
                    ElMessage({
                        type: "success",
                        message: "归档成功"
                    })
                    cliCC.value = false
                    anyP.value = false
                }
            })
        }
        onMounted(() => {
            getAllTop()
            window.addEventListener("click", mouseClick, true);
        })
        return {
            getAllTop,
            impDirId,
            impSpaceId,
            newDirId,
            newSpaceId,
            fileListGrid,
            fileMenu,
            folderList,
            fileTotal,
            getImage,
            rightClick,
            filterMouseCli,
            copyRow,
            clickRow,
            clickRowId,
            copyDirId,
            copySpaceId,
            copyFileId,
            copyFileSize,
            copyFileType,
            nameForm,
            copyFileName,
            cliCC,
            mouseClick,
            anyP,
            xz,
            yz,
            chooseSet,
            chooseSet1,
            thanks,
            FileTree,
            fileTrees,
            getCback,
            getChildren,
            copyOrMove,
            onlyView,
            editOnline,
            showPreview,
            ImgPreview,
            previewData,
            closeImgPreview,
            FileEdit,
            cancleHistoryPrew,
            historycopyRow,
            changeMsgClose,
            historyPrew,
            getCollects,
            collects,
            fileNameChange,
            editOnline,
            goLock,
            setImg,
            starFolder,
            delStarFolder
        }
    },
    components: {
        TransferModal,
        FileTree,
        ImgPreview,
        FileEdit,
        FileCollect,
    }
}
</script>

<style scoped lang="scss">
@import "@/assets/styles/my-common.scss";.bigBox {
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: flex-start;
    // align-items: center;
    flex-wrap: wrap;
}

.imgBox {
    width: 88px;
    height: 88px;
    text-align: center;
    line-height: 88px;
}

.flexBox {
    width: 104px;
    height: 148px;
    position: relative;
    .stars{
        position: absolute;
        top: 5%;
        right: 5%;
    }
}

.nameName {
    width: 100%;
    white-space: nowrap;
    /* 禁止文本换行 */
    overflow: hidden;
    /* 隐藏溢出部分文本 */
    text-overflow: ellipsis;
    /* 显示省略号 */
}

.setCli {
    width: 156px;
    max-height: auto;
    position: absolute;
    flex-wrap: 400;
    background-color: white;
    border: 1px solid gray;
    border-radius: 4px;
    z-index: 3;
    overflow-y: auto;
    /* // font-size: 16px; */
}

.setCli1 {
    width: 156px;
    height: auto;
    position: absolute;
    background-color: white;
    border: 1px solid gray;
    border-radius: 4px;
    z-index: 2;
    overflow-y: auto;
}

.chooseSet {
    width: 140px;
    height: 30px;
    line-height: 30px;
    margin: 5px auto;
    font-size: 13px;
    /* 
    display: flex;
    align-items: center;
    position: relative; */

}

.chooseSet img {
    margin-right: 4px;
    vertical-align: middle;
}

.chooseSet:hover {
    width: 140px;
    height: 30px;
    line-height: 30px;
    margin: 5px auto;
    font-size: 13px;
    background-color: #F5F7F9;
    /* color: white; */
}
</style>