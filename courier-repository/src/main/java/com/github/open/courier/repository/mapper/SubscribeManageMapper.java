package com.github.open.courier.repository.mapper;

import com.github.open.courier.core.transport.SubscribeManageDTO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 订阅服务管理Mapper
 *
 * @author LIJIAHAO
 */
@Mapper
@Repository
public interface SubscribeManageMapper {

    /**
     * 通过服务名查询订阅管理数据
     * @param service
     * @return
     */
    SubscribeManageDTO queryByService(String service);

    /**
     * 查询订阅管理全部数据
     * @return
     */
    List<SubscribeManageDTO> queryAll();

    /**
     * 查询订阅管理全部数据
     * @return
     */
    List<SubscribeManageDTO> queryDisableService();

    /**
     * 通过服务名称删除订阅管理数据
     * @param service
     */
    int deleteByService(String service);

    /**
     * 插入订阅管理数据
     * @param subscribeManageDTO
     */
    int insert(SubscribeManageDTO subscribeManageDTO);

    /**
     * 修改订阅管理数据
     * @param subscribeManageDTO
     */
    int update(SubscribeManageDTO subscribeManageDTO);

}
