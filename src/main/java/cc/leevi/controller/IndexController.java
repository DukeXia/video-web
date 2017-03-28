package cc.leevi.controller;

import cc.leevi.autoconfigure.FfmpegProperties;
import cc.leevi.common.Response;
import cc.leevi.model.Resource;
import cc.leevi.service.IndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by jiang on 2017-03-24.
 */
@RestController
public class IndexController {

    @Autowired
    private IndexService indexService;

    @Autowired
    private FfmpegProperties ffmpegProperties;

    @RequestMapping("/all")
    @ResponseBody
    public Response all() {
        Response<List<Resource>> response = new Response();
        response.setObj(indexService.getAll());
        return response;
    }

    @RequestMapping("/process")
    @ResponseBody
    public Response process() {
        Response<List<Resource>> response = new Response();
        indexService.processResources();
//        response.setObj();
        return response;
    }

}
