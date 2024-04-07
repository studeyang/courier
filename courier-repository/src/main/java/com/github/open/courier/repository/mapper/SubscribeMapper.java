package com.github.open.courier.repository.mapper;

import com.github.open.courier.core.transport.AssignMessagePushEnvRequest;
import com.github.open.courier.core.transport.ClearMessagePushEnvRequest;
import com.github.open.courier.core.transport.QuerySubscribeMetaDataRequest;
import com.github.open.courier.repository.entity.SubscribeEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 订阅关系Mapper
 */
@Mapper
@Repository
public interface SubscribeMapper {

    int insertBatch(Collection<SubscribeEntity> subscribes);

    int deleteByClusterAndService(@Param("cluster") String cluster, @Param("service") String service);

    Set<SubscribeEntity> listByService(@Param("cluster") String cluster, @Param("service") String service);

    Set<SubscribeEntity> listByServices(@Param("cluster") String cluster, @Param("collection") Collection<String> services);

    Set<SubscribeEntity> listAll();

    @Select("SELECT count(*) FROM courier_subscribe WHERE cluster = #{cluster}")
    int countCluster(@Param("cluster") String cluster);

    List<SubscribeEntity> findMetaDatasByPage(QuerySubscribeMetaDataRequest request);

    int assignMessagePushEnv(AssignMessagePushEnvRequest request);

    int clearMessagePushEnv(ClearMessagePushEnvRequest request);

    List<SubscribeEntity> listEnableService();

    List<SubscribeEntity> queryByService(String service);

    List<SubscribeEntity> checkConflict(@Param("groupIds") Set<String> groupIds);

    List<SubscribeEntity> selectEnvTag(@Param("cluster") String cluster, @Param("service") String service);

    List<SubscribeEntity> findAssignedGroupIdAndType();

    @Select("SELECT distinct cluster from courier_subscribe WHERE group_id = #{groupId} ")
    String whereCluster(@Param("groupId") String groupId);

    /* for unit test */

    @Delete("DELETE FROM `courier_subscribe`")
    void deleteAll();

}
