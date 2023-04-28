package com.douding.file.controller.admin;

import com.alibaba.fastjson.JSON;
import com.douding.server.domain.Test;
import com.douding.server.dto.FileDto;
import com.douding.server.dto.ResponseDto;
import com.douding.server.enums.FileUseEnum;
import com.douding.server.exception.BusinessException;
import com.douding.server.exception.BusinessExceptionCode;
import com.douding.server.exception.ValidatorException;
import com.douding.server.service.FileService;
import com.douding.server.service.TestService;
import com.douding.server.util.Base64ToMultipartFile;
import com.douding.server.util.UuidUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.util.List;

/*
    返回json 应用@RestController
    返回页面  用用@Controller
 */
@RequestMapping("/admin/file")
@RestController
public class UploadController {

    private static final Logger LOG = LoggerFactory.getLogger(UploadController.class);
    public static final String BUSINESS_NAME = "文件上传";
    @Resource
    private TestService testService;

    @Value("${file.path}")
    private String FILE_PATH;

    @Value("${file.domain}")
    private String FILE_DOMAIN;

    @Resource
    private FileService fileService;

    @RequestMapping("/upload")
    public ResponseDto upload(@RequestBody FileDto fileDto) throws Exception {
        if (ObjectUtils.isEmpty(fileDto)) {
            throw new ValidatorException("fileDto不能为空");
        }
        MultipartFile multipartFile = Base64ToMultipartFile.base64ToMultipart(fileDto.getShard());

        String localDirPath = FILE_PATH + FileUseEnum.getByCode(fileDto.getUse());
        File dirFile = new File(localDirPath);
        if (!dirFile.exists() && !dirFile.mkdirs()) {
            throw new Exception("文件夹创建失败，创建路径:" + localDirPath);
        }
        String fileFullPath = localDirPath + File.separator + fileDto.getKey() + "." + fileDto.getSuffix();

        String fileShardFullPath = fileFullPath + "." + fileDto.getShardIndex();

        multipartFile.transferTo(new File(fileShardFullPath));
        String relaPath = FileUseEnum.getByCode(fileDto.getUse()) + "/" + fileDto.getKey() + "." + fileDto.getSuffix();
        fileDto.setPath(relaPath);
        String shortUuid = UuidUtil.getShortUuid();
        fileDto.setId(shortUuid);
        fileService.save(fileDto);
        if (fileDto.getShardIndex().equals(fileDto.getShardTotal())) {
            fileDto.setPath(fileFullPath);
            merge(fileDto);
        }
        FileDto result = new FileDto();
        ResponseDto responseDto = new ResponseDto();
        result.setPath(FILE_DOMAIN + relaPath);
        responseDto.setContent(result);
        return responseDto;
    }

    //合并分片
    public void merge(FileDto fileDto) throws Exception {
        LOG.info("合并分片开始");
        try {
            String path = fileDto.getPath();
            OutputStream outputStream = new FileOutputStream(path, true);
            Integer shardTotal = fileDto.getShardTotal();
            for (Integer i = 1; i <= shardTotal; i++) {
                FileInputStream inputStream = new FileInputStream(path + "." + i);
                byte[] bytes = new byte[10 * 1024 * 1024];
                int len;
                while ((len = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, len);
                }
            }
        } catch (IOException e) {
            LOG.info("合并失败");
            throw new RuntimeException(e);
        }
        for (Integer i = 1; i <= fileDto.getShardTotal(); i++) {
            File file = new File(FILE_PATH + "." + i);
            file.delete();
        }
    }

    @GetMapping("/check/{key}")
    public ResponseDto check(@PathVariable String key) throws Exception {
        LOG.info("检查上传分片开始：{}", key);
        if (StringUtils.isEmpty(key)) {
            throw new BusinessException(BusinessExceptionCode.NOT_KEY);
        }
        FileDto fileDto = fileService.findByKey(key);
        ResponseDto responseDto = new ResponseDto();
        if (fileDto != null) {
            fileDto.setPath(FILE_DOMAIN + "/" + fileDto.getPath());
            responseDto.setContent(fileDto);
        }
        return responseDto;
    }
}//end class
