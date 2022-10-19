package com.example.springboot.controller;

import KNN.UsingController;
import KNN.utils.FiveTuple;
import KNN.utils.FourTuple;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Preview;
import com.example.springboot.mapper.PreviewMapper;
import org.ejml.simple.SimpleMatrix;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;


@RestController
@RequestMapping("/preview")
public class PreviewController {
    @Resource
    PreviewMapper PreviewMapper;
    @PostMapping
    public Result<?> save(@RequestBody Preview Preview){
        String str = Preview.getFile();
        String uploadFile = str.replaceAll("http://localhost:9090/files/","");//获取上传的文件名

        String trainFile = Preview.getTrainFile(); //训练数据文件名称

        if(Preview.getAlg().equals("KNN")){
            FourTuple<SimpleMatrix, Double, Double, Double> dataPack=
                    UsingController.trainingKnnControl
                            (7,System.getProperty("user.dir")+"/springboot/src/FILE/FileForTrain/"+trainFile,
                                    System.getProperty("user.dir")+"/springboot/src/FILE/Modules/Knn1",0.8);

            UsingController.predictControl(System.getProperty("user.dir")+"/springboot/src/FILE/Modules/Knn1",
                    System.getProperty("user.dir")+"/springboot/src/FILE/files/"+uploadFile);

            String newFileName = uploadFile.substring(0, uploadFile.lastIndexOf('.')) + "-result.csv";
            String predictFile = "http://localhost:9090/files/predict/"+newFileName;
            Preview.setResult(predictFile);
            Preview.setAccuracy(dataPack.second);
        }else {
            FiveTuple<double[], SimpleMatrix, Double, Double, Double> dataPack1 =
                    UsingController.trainingLogisticRegressionControl(300, 0.000001,
                            0, 0.1, 797,
                            System.getProperty("user.dir")+"/springboot/src/FILE/FileForTrain/"+trainFile,
                            System.getProperty("user.dir")+"/springboot/src/FILE/Modules/lgr2", 0.8);

            UsingController.predictControl(System.getProperty("user.dir")+"/springboot/src/FILE/Modules/lgr2",
                    System.getProperty("user.dir")+"/springboot/src/FILE/files/"+uploadFile);

            String newFileName = uploadFile.substring(0, uploadFile.lastIndexOf('.')) + "-result.csv";
            String predictFile = "http://localhost:9090/files/predict/"+newFileName;
            Preview.setResult(predictFile);
            Preview.setAccuracy(dataPack1.third);
        }
        PreviewMapper.insert(Preview);
        return Result.success();
    }

    @GetMapping
    public Result<?> find(){
        LambdaQueryWrapper<Preview>wrapper = Wrappers.lambdaQuery();
        List<Preview> pre = PreviewMapper.selectList(wrapper);
        return Result.success(pre);
    }

}
