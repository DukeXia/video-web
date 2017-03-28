package cc.leevi.service;

import cc.leevi.ffmpeg.VideoHandler;
import cc.leevi.mapper.ResourceMapper;
import cc.leevi.model.Resource;
import cc.leevi.model.ResourceExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jiang on 2017-03-24.
 */
@Service
public class IndexService {

    @Autowired
    private ResourceMapper resourceMapper;

    @Autowired
    private VideoHandler videoHandler;

    public List<Resource> getAll(){
        List<Resource> all = resourceMapper.selectByExample(new ResourceExample());
        List<Resource> tree = new ArrayList<>();
        for(Resource root : all){
            if (root.getParent()==0){
                root.setChildren(getChildren(root.getId(),all));
                tree.add(root);
            }
        }
        return tree;
    }

    private List<Resource> getChildren(Integer parent,List<Resource> all){
        List<Resource> children = new ArrayList<>();
        for(Resource resource : all){
            if(resource.getParent().equals(parent)){
                resource.setChildren(getChildren(resource.getId(),all));
                children.add(resource);
            }
        }
        return children;
    }

    public void processResources(){
        videoHandler.process();
    }
}
