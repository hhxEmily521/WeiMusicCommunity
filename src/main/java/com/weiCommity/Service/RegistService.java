package com.weiCommity.Service;

import com.weiCommity.Dao.LoginDao;
import com.weiCommity.Dao.RegistDao;
import com.weiCommity.Model.Login;
import com.weiCommity.Model.UserExtend;
import com.weiCommity.Model.UserTFWork;
import com.weiCommity.SpringConfigration;
import com.weiCommity.Util.HttpJson;
import com.weiCommity.Util.MD5.MD5String;
import com.weiCommity.Util.StaticVar;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Created by uryuo on 17/4/17.
 */
@Service
public class RegistService {
    @Autowired
    LoginDao loginDao;
    @Autowired
    RegistDao registDao;

    //注册账户名
    public String registUserWithType(Login thisUser ,String style) throws Exception {
        //生成uuid
        UUID uuid = UUID.randomUUID();
        //密码加密
        MD5String md5 = new MD5String(thisUser.getUPwd());

        thisUser.setUPwd(md5.getCalcString());
        thisUser.setUUuid(uuid.toString());

        //检查重复用户

        String testUserRe = "exist";
        testUserRe = loginDao.getUserUUID(thisUser.getUAName(),style);
        if (testUserRe!=null)
            return "-1";//用户名已存在

        //style重填装
        if (style.equals("M")){
            thisUser.setUMail(thisUser.getUAName());
            thisUser.setUAName(null);
        }else if (style.equals("T")){
            thisUser.setUTel(thisUser.getUAName());
            thisUser.setUAName(null);
        }
        boolean reFlag =  loginDao.registUser(thisUser);
        if (!reFlag)
            return "-2"; //注册出现了问题
        return thisUser.getUUuid();
    }

    //用相关类注册常用工种
    public void registFreqTOWByWorkId(String UUuid,List<Object> insertID) throws Exception {
        for (int i=0;i<insertID.size();i++){
            UserTFWork thisTF = new UserTFWork();
            thisTF.setIsFreq(1);
            thisTF.setUUuid(UUuid);
            thisTF.setWorkId((String) insertID.get(i));;
            thisTF.setUWid(UUID.randomUUID().toString());

            registDao.insertWTByUUuid(thisTF);
        }
    }

    //注册扩展信息，带着头像(用固定编码格式编码)
    public String registExtandInfo(UserExtend userExtend,String imgStr) throws Exception{


        //对头像的存储和处理 路径是UserSpace/UserId/
        String perfixPath= StaticVar.getPreFilePath();
        String imgPath = "";
        File tarPath;
        //检测是否传了空值
        if (userExtend.getUHeadImg().isEmpty()){
            imgPath=imgPath = "CommonSpace/default_personal_image.png";

        }
        else{
            File oraginalPath = new File(userExtend.getUHeadImg());
            imgPath =  "UserSpace/"+userExtend.getUUuid() + "/" + oraginalPath.getName();
             tarPath = new File( "classpath:/FileSpace/"+ imgPath);
            FileUtils.writeByteArrayToFile(tarPath,imgStr.getBytes("ISO-8859-1"));
        }



        userExtend.setUHeadImg(imgPath);
        //调用Dao层存储
        registDao.insertUserExtend(userExtend);
        return imgPath;
    }



}
