package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessHistoryMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.model.po.MediaProcessHistory;
import com.xuecheng.media.service.MediaFileProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class MediaFileProcessServiceImpl implements MediaFileProcessService {

    @Resource
    MediaFilesMapper mediaFilesMapper;
    @Resource
    MediaProcessMapper mediaProcessMapper;

    @Resource
    MediaProcessHistoryMapper mediaProcessHistoryMapper;

    @Override
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count) {
        return mediaProcessMapper.selectListByShardIndex(shardIndex,shardTotal,count);
    }

    @Override
    public boolean startTask(long id) {
        int result = mediaProcessMapper.startTask(id);
        return result > 0;
    }

    @Override
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg) {
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        //任务不存在直接返回
        if (mediaProcess == null){
            return ;
        }

        //处理失败，更新任务处理结果
        LambdaQueryWrapper<MediaProcess> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MediaProcess::getId,taskId);
        if (status.equals("3")){
            MediaProcess updateMediaProcess = new MediaProcess();
            BeanUtils.copyProperties(queryWrapper,updateMediaProcess);
            updateMediaProcess.setStatus(status);
            updateMediaProcess.setErrormsg(errorMsg);
            updateMediaProcess.setFailCount(mediaProcess.getFailCount()+1);
            mediaProcessMapper.update(updateMediaProcess,queryWrapper);
            log.debug("更新任务处理状态为失败，任务信息:{}",updateMediaProcess);
            return ;
        }

        //处理成功，更新任务处理结果
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
        if (mediaFiles != null){
            mediaFiles.setUrl(url);
            mediaFilesMapper.updateById(mediaFiles);
        }

        MediaProcess updateMediaProcess = new MediaProcess();
        BeanUtils.copyProperties(queryWrapper,updateMediaProcess);
        updateMediaProcess.setStatus(status);
        updateMediaProcess.setUrl(url);
        updateMediaProcess.setFinishDate(LocalDateTime.now());
        mediaProcessMapper.update(updateMediaProcess,queryWrapper);
        log.debug("更新任务处理状态为成功，任务信息:{}",updateMediaProcess);

        //添加到历史记录
        MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
        BeanUtils.copyProperties(mediaProcess, mediaProcessHistory);
        mediaProcessHistoryMapper.insert(mediaProcessHistory);
        //删除mediaProcess
        mediaProcessMapper.deleteById(mediaProcess.getId());

    }
}
