<template>
    <div>
        <el-dialog v-model="treeOpen" :title="options" width="30%" :close-on-click-modal="false">
            <el-tree :data="treeData" :props="defaultProps" @node-click="handleNodeClick" />
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="backToFile">取消</el-button>
                    <el-button type="primary" @click="sureCopy">确认</el-button>
                </span>
            </template>
        </el-dialog>
    </div>
</template>

<script>
import { ref, onMounted, toRefs } from 'vue';
import documents from '../../../api/document/document';
import myfile from '../../../api/myfile/myfile'
import { ElMessage } from 'element-plus'
export default {
    props: {
        fileTrees: {
            type: Boolean,
            required: true
        },
        newSpaceId: {
            type: Number,
            required: true
        },
        fileId: {
            type: Number,
            required: true
        },
        copyOrMove: {
            type: Number,
            required: true
        }
    },
    setup(props, { emit }) {
        let { backToFile, sureCopy } = toRefs(props)
        let result = props.fileTrees
        let spaceIds = props.newSpaceId
        let fileIds = props.fileId
        let changeMes = props.copyOrMove - 0
        let dirIds = ref('')
        let treeOpen = ref(false)
        let options = ref("复制到")
        let treeData = ref([])
        let defaultProps = ref({
            label: "label",
            value: "id",
            disable: false
        })
        //获取文件树
        function getFileTree() {
            documents.fileTree(3).then(res => {
                treeData.value = [res]
            })
        }
        function modalOpen() {
            if (result) {
                treeOpen.value = result
            }
        }
        function everyWhere() {
            treeOpen.value = false
            emit("getChildren", treeOpen.value)
        }
        function knowEvery() {
            if (changeMes === 0) {
                myfile.fileCopy({ dirId: dirIds.value - 0, docId: fileIds, spaceId: spaceIds }).then(res => {
                    if (res.code === 200) {
                        ElMessage({
                            type: "success",
                            message: "复制成功"
                        })
                    }
                })
            } else if (changeMes === 1) {
                myfile.fileMove({ dirId: dirIds.value - 0, docId: fileIds, spaceId: spaceIds }).then(res => {
                    if (res.code === 200) {
                        ElMessage({
                            message: "移动成功",
                            type: "success"
                        })
                    }
                })
            }
            treeOpen.value = false
            emit("getChildren", treeOpen.value)
        }
        function handleNodeClick(data) {
            dirIds.value = data.id
        }
        function changes() {
            if (changeMes === 1) {
                options.value = "移动到"
            } else if (changeMes === 0) {
                options.value = '复制到'
            }
        }
        onMounted(() => {
            modalOpen()
            getFileTree()
            changes()
        })
        return {
            getFileTree,
            result,
            treeOpen,
            options,
            backToFile: everyWhere,
            sureCopy: knowEvery,
            treeData,
            handleNodeClick,
            defaultProps,
            dirIds,
            spaceIds,
            fileIds,
            changeMes,
        }
    },
}
</script>

<style scoped></style>