<template>
    <div>
        <div>
            <el-dialog v-model="askTo" title="扩容申请" width="30%" @close="backTo">
                <el-form :model="spaceForm">
                    <el-form-item label="新容量">
                        <el-input-number v-model="spaceForm.newCup" :min="1" :max="10" />
                    </el-form-item>
                    <el-form-item label="申请理由">
                        <el-input v-model="spaceForm.newTalk" maxlength="150" placeholder="请输入申请理由" show-word-limit
                            type="textarea" />
                    </el-form-item>
                </el-form>
                <template #footer>
                    <span class="dialog-footer">
                        <el-button @click="backTo">取消</el-button>
                        <el-button type="primary" @click="sureAsk">
                            确定
                        </el-button>
                    </span>
                </template>
            </el-dialog>
        </div>
    </div>
</template>

<script>
import { ref, onMounted, toRef } from 'vue'
import { ElMessage } from 'element-plus';
import fileSpace from '../../../api/filespace/fileSpace';
export default {
    props: {
        spaces: {
            type: Boolean,
            required: true
        },
        impSpaceId: {
            type: Number,
            required: true
        }
    },
    setup(props, { emit }) {
        let { backTo, sureAsk } = toRef(props)
        let result = props.spaces
        let spid = props.impSpaceId
        let askTo = ref(false)
        let spaceForm = ref({
            newCup: 1,
            newTalk: ""
        })
        function inlineList() {
            askTo.value = result
        }
        function to() {
            askTo.value = false
            emit("getSpaceC", askTo.value)
        }
        function  addSpace() {
         fileSpace.addSpace({
                spaceId: spid - 0,
                expandCap: spaceForm.value.newCup - 0,
                expandReason: spaceForm.value.newTalk
            }).then(res => {
                console.log(res,'ssq');
            })
            askTo.value = false
            emit("getSpaceC", askTo.value)
        }
        onMounted(() => {
            inlineList()
        })
        return {
            askTo,
            spaceForm,
            backTo: to,
            sureAsk: addSpace,
            inlineList
        }
    }
}
</script>

<style lang="scss" scoped></style>