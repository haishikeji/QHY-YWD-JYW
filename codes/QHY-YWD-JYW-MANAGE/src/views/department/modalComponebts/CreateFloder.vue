<template>
    <div>
        <div>
            <el-dialog v-model="addBoser" :close-on-click-modal="false" :title="upFolderData?'修改':'新建文件夹'" width="30%" @close="diClose">
                <el-form :model="boserForm" label-width="120px">
                    <el-form-item label="目录名称" >
                        <el-input v-model="boserForm.dirName" placeholder="请输入不多于32位字符" maxlength='32'/>
                    </el-form-item>
                    <el-form-item label="目录分类" v-if="free || (!free && thisType.dirType != '2' ) || (!free && upFolderData )">
                        <el-radio-group :disabled="upFolderData" v-model="boserForm.dirType">
                            <el-radio :label="1">公开目录</el-radio>
                            <el-radio :label="2">组织目录</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item v-if="!upFolderData" label="是否使用模板">
                       <el-switch @change="switchChange" v-model="boserForm.useTemp" active-value="Y" inactive-value="N" />
                       <el-button v-if="boserForm.useTemp === 'Y'" style="margin-left:10px;" @click="checkTemp" type="info" plain>{{preTempTitle || '点击选择模板'}}</el-button>
                    </el-form-item>
                   <template v-if="(boserForm.dirType === 2 || thisType.dirType == '2')">
                        <template v-if="!upFolderData && (free || (!free && thisType.dirType != '2' ))">
                            <el-form-item label="是否加密" v-if="!FolderRole?.levelCode || free">
                                <el-switch v-model="boserForm.isEncrypt" active-value="Y" inactive-value="N" />
                            </el-form-item>
                            <el-form-item label="加密等级" v-if=" (!FolderRole?.levelCode || free) && boserForm.isEncrypt === 'Y'">
                                <el-radio-group v-model="boserForm.encryptLevel">
                                    <el-radio :label="item.levelCode" v-for="item in levelList" :key="item">{{item.levelName}}</el-radio>
                                </el-radio-group>
                            </el-form-item>
                            <el-form-item label="私有访问码" v-if=" (!FolderRole?.levelCode || free) && boserForm.isEncrypt === 'Y' && needCode">
                                <el-input v-model="boserForm.accessCode" type="password" show-password />
                            </el-form-item>
                        </template>
                        <template v-if="free || (!free && thisType.dirType != '2' )||(!free && upFolderData )">
                            <el-form-item v-if="!FolderRole?.levelCode || free || upFolderData" label="成员权限">
                                <el-checkbox-group v-model="boserForm.memberRoles">
                                    <el-checkbox v-for="item in dir_rules" :key="item.value"  :label="item.value">{{item.label}}</el-checkbox>
                                </el-checkbox-group>
                            </el-form-item>
                            <el-form-item label="选择成员" v-if="boserForm.dirType === 2">
                            </el-form-item>
                            <CreateTasnsfer v-if="boserForm.dirType === 2" :checkUser="upFolderData?upFolderData.users:null" @setUsers="setUsers" :upFolderData="upFolderData" :openTrans="openTrans"></CreateTasnsfer>
                        </template>
                   </template>
                </el-form>
                <template #footer>
                    <span class="dialog-footer">
                        <el-button @click="addBack">取消</el-button>
                        <el-button v-if="upFolderData" type="primary" @click="sureUpBoser">
                            确认
                        </el-button>
                        <el-button v-else type="primary" @click="sureAddBoser">
                            确认
                        </el-button>
                    </span>
                </template>
            </el-dialog>
            <MenuTempList v-if="showTempList" :showTempList='showTempList' @getTempPreview="getTempPreview" @getAddTempId='getAddTempId' @closeTempList="closeTempList"></MenuTempList>
            <PreviewMenuList v-if="showPreTemp" :showPreTemp="showPreTemp" :preTemp="preTemp" @closePreTemp="closePreTemp"></PreviewMenuList>
        </div>
    </div>
</template>

<script>
import { ref, onMounted, toRef,toRaw,inject ,watch} from 'vue';
import documents from '../../../api/document/document';
import MenuTempList from '@/components/MenuTempList/MenuTempList.vue';
import PreviewMenuList from '@/components/PreviewMenuList/PreviewMenuList.vue';
import {listUser} from '../../../api/biz/user';
import { getLevel } from '@/api/level/level.js';
import { ElMessage } from 'element-plus';
import { sm3 ,sm2} from "sm-crypto";
import CreateTasnsfer from '../components/createTasnsfer.vue'

export default {
    props: {
        folderCase: {
            type: Boolean,
            required: true
        },
        oneOrTwo: {
            type: Number,
            required: true
        },
        impDirId: {
            type: Number,
            required: true
        },
        impSpaceId: {
            type: Number,
            required: true
        },
        newDirId: {
            type: Number,
            required: true
        },
        newSpaceId: {
            type: Number,
            required: true
        },
        thisType: {
            type: String,
            required: true
        },
        thisFolderRole: {
            type: Object,
            required: true,
            default: ()=>{},
        },
        thisRole: {
            type: Object,
            // required: true,
            default: ()=>{},
        },
        upFolderData: {
            type: Object,
            // required: true,
            default: ()=>{},
        },
        refreshFile: Function,
        getFirstFolder: Function,
    },
    setup(props, { emit }) {
        const {
            proxy
        } = getCurrentInstance();
        const {
            dir_rules
        } = proxy.useDict('dir_rules');
        let { addBack, sureAddBoser, diClose } = toRef(props)
        const upFirstFolderData = inject("upFirstFolderData");
        let result = props.folderCase
        let free = props.oneOrTwo
        let spId = props.impSpaceId
        let drId = props.impDirId
        let newspid = props.newSpaceId
        let newdrid = props.newDirId
        let upFolderData = props.upFolderData
        const FolderRole =free?'': props.thisFolderRole
        const thisRole = free?'': props.thisRole
        const thisType = free?'': props.thisType
        const users = ref()
        const openTrans = ref(true)
        // console.log("FolderRole",FolderRole);
        // console.log("thisRole",thisRole);
        // console.log("thisType",thisType);
        // console.log("upFolderData",upFolderData);
        let addBoser = ref(false)
        let boserForm = ref({
            dirName: upFolderData?upFolderData.dirName:"",
            useTemp:'N',
            dirType:upFolderData?2:1,
            isEncrypt:upFolderData?upFolderData.isEncrypt:"Y",
            encryptLevel:upFolderData?upFolderData.encryptLevel:"L1",
            memberRoles:upFolderData?upFolderData.dirRole.split(','):[],
            users:upFolderData?upFolderData.users:[],
            accessCode:upFolderData?upFolderData.accessCode:'',
            remark:upFolderData?upFolderData.remark:''
        })
        const levelList = ref([])
        const needCode = ref(false) // 需要私有码
        const showTempList = ref(false)
        const tempId = ref()
        const showPreTemp = ref(false)
        const preTemp = ref()
        const preTempTitle = ref('')
        function inital() {
            // console.log(free, 'free');
            addBoser.value = result
        }
        function backTo() {
            addBoser.value = false
            emit("getChildrenC", addBoser.value)
        }
        function addFloder() {
             if(boserForm.value.useTemp === "Y" && !tempId.value){
                ElMessage({
                    type: "error",
                    message: "请选择模板"
                })
                return
            }
            if (boserForm.value.dirName === '') {
                ElMessage({
                    type: "error",
                    message: "目录名称不能为空"
                })
            } else {
                if (free === 1) {
                    if (boserForm.value.accessCode === '' && needCode.value) {
                        ElMessage({
                            type: "error",
                            message: "私有访问码不能为空"
                        })
                        return
                    }
                    let query = {}
                    if(boserForm.value.dirType === 2){ // 组织目录
                        query = {
                            "dirName": boserForm.value.dirName,
                            "parentId": drId - 0,
                            "spaceId": spId - 0,
                            "dirType":thisType.dirType=='2'?'2':boserForm.value.dirType,
                            "dirRole":boserForm.value.memberRoles.join(','),
                            "users":boserForm.value.users,
                            "isEncrypt":boserForm.value.isEncrypt,
                            "encryptLevel":boserForm.value.isEncrypt == "Y"?boserForm.value.encryptLevel:null,
                            "accessCode":boserForm.value.accessCode?sm3(boserForm.value.accessCode):''
                        }
                    }else{
                        query = {
                            "dirName": boserForm.value.dirName,
                            "parentId": drId - 0,
                            "spaceId": spId - 0,
                            "dirType":thisType.dirType=='2'?'2':boserForm.value.dirType,
                            
                    }
                    }
                     if(boserForm.value.useTemp == 'N'){
                        documents.addDocument(
                        query
                        ).then(res => {
                            if (res.code === 200) {
                            ElMessage({
                                message: '添加一级目录成功',
                                type: 'success',
                            })
                        }
                        addBoser.value = false
                        emit("getChildrenC", addBoser.value)
                        props.getFirstFolder()
                        })
                    }else{
                        documents.addDocumentByTemp(
                        query,tempId.value
                        ).then(res => {
                            if (res.code === 200) {
                            ElMessage({
                                message: '添加一级目录成功',
                                type: 'success',
                            })
                        }
                        addBoser.value = false
                        emit("getChildrenC", addBoser.value)
                        props.getFirstFolder()
                        })
                    }
                } else {
                    let query = {}
                    if(FolderRole?.levelCode || thisRole.length ||thisType.dirType == "2"){
                        //父级有权限自动继承 没有就用添加的值
                        query={
                            "dirName": boserForm.value.dirName,
                            "parentId": newdrid - 0,
                            "spaceId": newspid - 0,
                            "dirType":2,
                            "isEncrypt":"Y",
                            "encryptLevel":FolderRole?.levelCode,
                            "users":thisType.dirType === "1"?boserForm.value.users:users.value,
                            "dirRole":thisRole.length?thisRole.join(','):'',
                            "accessCode":FolderRole.accessCode,
                            "remark":'extend'
                        }
                        // console.log('query',query);
                        // console.log('FolderRole',FolderRole);
                    }else{
                        if (boserForm.value.accessCode === '' && needCode.value) {
                            ElMessage({
                                type: "error",
                                message: "私有访问码不能为空"
                            })
                            return
                        }
                        query={
                            "dirName": boserForm.value.dirName,
                            "parentId": newdrid - 0,
                            "spaceId": newspid - 0,
                            "dirType":boserForm.value.dirType,
                            "isEncrypt":boserForm.value.isEncrypt,
                            "encryptLevel":(boserForm.value.isEncrypt == "Y" && boserForm.value.dirType == "2")?boserForm.value.encryptLevel:null,
                            "users":thisType.dirType === "1"?boserForm.value.users:users.value,
                            "dirRole":thisType.dirType === "1"?boserForm.value.memberRoles.join(','):thisRole.join(','),
                            "accessCode":boserForm.value.accessCode?sm3(boserForm.value.accessCode):''
                        }
                    }
                    if(boserForm.value.useTemp == 'N'){
                        documents.addDocument(
                        query
                        ).then(res => {
                            if (res.code === 200) {
                            ElMessage({
                                message: '添加目录成功',
                                type: 'success',
                            })
                        }
                        addBoser.value = false
                        emit("getChildrenC", addBoser.value)
                        props.refreshFile()
                        })
                    }else{
                        documents.addDocumentByTemp(
                        query,tempId.value
                        ).then(res => {
                            if (res.code === 200) {
                            ElMessage({
                                message: '添加目录成功',
                                type: 'success',
                            })
                        }
                        addBoser.value = false
                        emit("getChildrenC", addBoser.value)
                        props.refreshFile()
                        })
                    }
                }
            }
        }
        const sureUpBoser = ()=>{
             if (boserForm.value.dirName === '') {
                ElMessage({
                    type: "error",
                    message: "目录名称不能为空"
                })
                return
            }
            let query  = {}
            if(free === 1){
                if (boserForm.value.accessCode === '' && needCode.value) {
                        ElMessage({
                            type: "error",
                            message: "私有访问码不能为空"
                        })
                        return
                    }
                 query ={
                    "dirName": boserForm.value.dirName,
                    "parentId": drId - 0,
                    "spaceId": spId - 0,
                    'dirId':upFolderData.dirId,
                    "dirType":thisType.dirType=='2'?'2':boserForm.value.dirType,
                    "dirRole":boserForm.value.memberRoles.join(','),
                    "users":boserForm.value.users,
                    "isEncrypt":boserForm.value.isEncrypt,
                    "encryptLevel":boserForm.value.isEncrypt == "Y"?boserForm.value.encryptLevel:null,
                    "accessCode":boserForm.value.accessCode?sm3(boserForm.value.accessCode):'',
                    "remark":boserForm.value.remark?boserForm.value.remark:'',
                }
            }else{
                 query ={
                    "dirName": boserForm.value.dirName,
                    "parentId": newdrid - 0,
                    "spaceId": newspid - 0,
                    'dirId':upFolderData.dirId,
                    "dirType":thisType.dirType=='2'?'2':boserForm.value.dirType,
                    "dirRole":boserForm.value.memberRoles.join(','),
                    "users":boserForm.value.users,
                    "isEncrypt":boserForm.value.isEncrypt,
                    "encryptLevel":boserForm.value.isEncrypt == "Y"?boserForm.value.encryptLevel:null,
                    "accessCode":boserForm.value.accessCode?sm3(boserForm.value.accessCode):'',
                    "remark":boserForm.value.remark?boserForm.value.remark:'',
                }
            }
            
                // console.log('query',query);
                documents.editFolerDocument(
                       query
                    ).then(res => {
                        if (res.code === 200) {
                            ElMessage({
                                message: '修改目录成功',
                                type: 'success',
                            })
                        }
                        addBoser.value = false
                        upFirstFolderData(query)
                        emit("getChildrenC", addBoser.value)
                        props.refreshFile(null,true)
                    })
        }
        const getLevelList = async ()=>{
            const res = await getLevel()
            if(res.rows){
                levelList.value = res.rows
            }
            // console.log(res);
        }
        const setUsers = (arr)=>{
            const oldArr = toRaw(arr)
            // boserForm.value.users = oldArr.map(item=>toRaw(item))
            const newArr = oldArr.map(item=>toRaw(item))
            boserForm.value.users = newArr.map(item=>{
                delete item['disabled']
                return item
            })
            // console.log('newArr',boserForm.value.users);
        }
        const getUser =async  ()=>{
           const  res = await listUser({dirId:thisType.dirId})
            //  console.log('getUser',res);
             users.value = res.rows
        }
        const switchChange = (val)=>{
            if(val === "Y"){
                showTempList.value = true
            }else{
                preTempTitle.value = ''
                tempId.value = null
                showTempList.value = false
            }
        }
        const closeTempList = ()=>{
            showTempList.value = false
        }
        const closePreTemp = ()=>{
            showPreTemp.value = false
        }
        const getAddTempId = (id,name)=>{
            tempId.value = id
            preTempTitle.value = name
            showTempList.value = false
        }
        const getTempPreview = (item)=>{
            showPreTemp.value = true
    
            preTemp.value = JSON.parse(item.tmplContent)
        }
        const checkTemp = ()=>{
            showTempList.value = true
        }
        //监听加密等级
        watch(() => boserForm.value.encryptLevel, (newValue, oldValue) => {
            const thisRole = levelList.value.find(item=>item.levelCode == newValue)
            // console.log('thisRole',thisRole);
            if(thisRole.roles.l0700){
                needCode.value = true
            }else{
                needCode.value = false
            }
        }, {
        deep: true
        });
        onMounted(() => {
            inital()
            getLevelList()
            if(thisType.dirType === "2"){
                getUser()
            }
        })
        return {
            addBoser,
            boserForm,
            inital,
            addBack: backTo,
            sureAddBoser: addFloder,
            diClose: backTo,
            spId,
            drId,
            newdrid,
            newspid,
            levelList,
            getLevelList,
            FolderRole,
            free,
            openTrans,
            setUsers,
            dir_rules,
            proxy,
            thisRole,
            thisType,
            getUser,
            users,
            sureUpBoser,
            upFirstFolderData,
            needCode,
            switchChange,
            showTempList,
            closeTempList,
            getAddTempId,
            tempId,
            getTempPreview,
            showPreTemp,
            preTemp,
            closePreTemp,
            preTempTitle,
            checkTemp
        }
    },
    components: {
        CreateTasnsfer,
        MenuTempList,
        PreviewMenuList
    },
}
</script>

<style lang="scss" scoped></style>