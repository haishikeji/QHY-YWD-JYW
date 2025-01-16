<template>
    <div>
        <div>
            <!-- flex盒子 -->
            <div class="bigBox" style="height: 37vh; overflow: auto;" v-if="folderList?.length">
                <template v-for="(item, index) in folderList" :key="index">
                    <!-- 循环盒子 -->
                    <div class="flexBox" @contextmenu="rightClick(item,$event)" @click="folderClickGrid(item)">
                        <!-- 图片 -->
                        <div class="imgBox">
                            <img :src="item.dirType=='1'?fileBox:ordinaryList"
                              alt="" style="vertical-align: middle;cursor: pointer;">
                        </div>
                        <!-- 名称 -->
                        <div class="nameBox">
                            <el-tooltip
                            class="box-item"
                            effect="dark"
                            placement="top"
                            :show-after="1000"
                            :content="item.dirName"
                        ><p class="nameName" style="cursor: pointer;">{{ item.dirName }}</p></el-tooltip>
                        </div>
                        <div class="stars">
                             <el-icon v-if="item.isFavorite == 'N'" @click.stop="starFolder(item,true)" style="margin-right: 7px;">
                                <Star />
                            </el-icon>
                            <img v-else @click.stop="delStarFolder(item,true)" src="@/assets/images/yellowstar.png" alt="">
                        </div>
                    </div>
                </template>
                <!-- 右键唤出的菜单 -->
                <div class="rightList" v-if="folderVisible"
                    :style="{ left: folderleft + 30 + 'px', top: foldertop - 50 + 'px' }">
                    <div class="menu_item" @click="fatherOpen(null, null)" style="cursor: pointer;">
                        <img src="@/assets/images/trash.png" alt="" style="vertical-align: middle;margin-right: 5px;"/>
                        <span>打开</span>
                    </div>
                    <div class="menu_item" @click="fatherRest(null, null)" style="cursor: pointer;">
                        <img src="@/assets/images/textbox.png" alt="" style="vertical-align: middle;margin-right: 5px;"/>
                        <span>重命名</span>
                    </div>
                    <div class="menu_item" @click="fatherCollect" style="cursor: pointer;">
                        <img src="@/assets/images/collect.png" alt="" style="vertical-align: middle;margin-right: 5px;"/>
                        <span>收藏</span>
                    </div>
                </div>
            </div>
            <div v-else  class="none-data">暂无数据</div>
        </div>
    </div>
</template>

<script>
import { ref, onMounted , toRaw,watch} from "vue"
import { ElMessage } from "element-plus";
import myfile from "../../../api/myfile/myfile";
import documents from "../../../api/document/document";
import ImgFile from "../jsComponents/ImgFile";
import PalaceGridFile from "./PalaceGridFile.vue";
import fileBox from "@/assets/images/fileBox.png";
import ordinaryList from "@/assets/images/ordinaryList.png";
export default {
    props: {
        folderClick: {
            type: Function,
            // required: true,
        },
        restName: {
            type: Function,
            required: true
        },
        collectFolder: {
            type: Function,
            required: true
        },
        getAllTop: {
            type: Function,
            required: true
        },
        thisFolder: {
            type: Object,
            required: true
        },
        folderList: {
            type: Object,
            required: true
        }

    },
    setup(props, { emit }) {
        let impDirId = ref(0)
        let impSpaceId = ref(0)
        let newSpaceId = ref(0)
        let newDirId = ref(0)
        let topPath = ref("")
        let fileList = ref([])
        let folderListGrid = ref([])
        let fileTotal = ref(0)
        let fileMenu = ref([])
        let folderleft = ref(0)
        let foldertop = ref(0)
        let folderVisible = ref(false)
        let copyDir = ref(0)
        let copySpa = ref(0)
        let copyNma = ref("")
        let copyFir = ref("")
        let folderList = ref(toRaw(props.folderList))
        // 获取文件夹，中栏，文件
        function getAllTop() {
            documents.getTop(1).then(res => {
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
                const fileId =props.thisFolder.dirId?props.thisFolder.dirId:userMe[0].dirId - 0
                documents.getALLdocument({ parentId: fileId, spaceId: userMe[0].spaceId, pageNum: obj.pageNum, pageSize: obj.pageSize }).then(res => {
                    fileMenu.value = res.data
                    folderListGrid.value = res.data
                })
                // myfile.getById(userMe[0].dirId - 0, obj).then(res => {
                //     if (res.code === 200) {
                //         fileList.value = res.rows
                //         fileTotal.value = res.total
                //         if (fileTotal.value / 10 != 1) {
                //             let x = Math.trunc(fileTotal.value / 10) + 1
                //             for (var i = 1; i <= x; i++) {
                //                 obj.pageNum = i
                //                 myfile.getById(userMe[0].dirId - 0, obj).then(res => {
                //                     fileList.value = fileList.value.concat(res.rows)
                //                     const uniqueFileList = Array.from(new Set(fileList.value.map(item => item.docId)))
                //                         .map(docId => fileList.value.find(item => item.docId === docId));
                //                     // 合并数组并更新 fileList.value
                //                     fileList.value = uniqueFileList
                //                 })
                //             }
                //         }
                //     }
                // })
            })
        }
        // function rightClick(row, num, eve) {
        //     eve.preventDefault();
        //     eve.stopPropagation();
        //     folderleft.value = eve.pageX
        //     foldertop.value = eve.pageY
        //     copyDir.value = row.dirId
        //     copySpa.value = row.spaceId
        //     copyNma.value = row.dirName
        //     copyFir.value = row.dirName
        //     if (folderVisible.value) {
        //         folderVisible.value = false
        //     } else {
        //         folderVisible.value = true
        //     }
        // }
        const rightClick = (row,e)=>{
        emit("folderRClick", row,null,e)
       }
        function mouseClick() {
            folderVisible.value = false
        }
        function fatherOpen(a, b) {
            documents.getALLdocument({
                parentId: copyDir.value - 0,
                spaceId: copySpa.value - 0
            }).then(res => {
                folderListGrid.value = res.data
                if (res.data.length === 0) {
                    myfile.getById(copyDir.value).then(res => {
                        if (res.code === 200) {
                            fileList.value = res.rows
                        }
                    })
                } else {
                    myfile.getById(copyDir.value).then(res => {
                        if (res.code === 200) {
                            fileList.value = res.rows
                        }
                    })
                }
            })
        }
        function fatherCollect() {
            emit("folderBack", copyNma.value, copyFir.value)
            props.collectFolder()
        }
        function fatherRest(a, b) {
            props.restName(a, b)
            emit('folderBack', copyDir.value, copySpa.value, copyNma.value)
        }
        const folderClickGrid = (item) => {
            // getAllTop()
            emit("folderClick", item)
           console.log('item', item)
        }
        const starFolder = (item) => {
            emit("collectByStar", item,true)
        }
        const delStarFolder = (item) => {
            emit("delCollect", item,true)
        }
        onMounted(() => {
            // getAllTop()
            window.addEventListener("click", mouseClick, true);
        })
        watch(() => props.folderList, (newValue, oldValue) => {
            console.log('newValue 发生改变了', newValue, oldValue);
            folderList.value = newValue

            }, {
            immediate: true,
            deep: true
        });
        return {
            folderClickGrid,
            getAllTop,
            impDirId,
            impSpaceId,
            newDirId,
            newSpaceId,
            fileList,
            fileMenu,
            folderListGrid,
            fileTotal,
            rightClick,
            folderleft,
            foldertop,
            folderVisible,
            mouseClick,
            fatherCollect,
            fatherOpen,
            fatherRest,
            copyDir,
            copySpa,
            copyNma,
            copyFir,
            ordinaryList,
            fileBox,
            folderList,
            starFolder,
            delStarFolder
        }
    },
    components:{
        PalaceGridFile,
    }
}
</script>
<style scoped lang="scss">
@import "@/assets/styles/my-common.scss";
.bigBox {
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: flex-start;
    // align-items: center;
    flex-wrap: wrap;
    padding-top: 16px;
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
    text-align: center;
    white-space: nowrap;
    /* 禁止文本换行 */
    overflow: hidden;
    /* 隐藏溢出部分文本 */
    text-overflow: ellipsis;
    /* 显示省略号 */
}

.rightList {
    width: 100px;
    height: auto;
    position: absolute;
    background-color: white;
    border: 1px solid black;
    border-radius: 5px;
}
</style>