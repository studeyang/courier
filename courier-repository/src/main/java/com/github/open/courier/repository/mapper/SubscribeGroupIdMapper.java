package com.github.open.courier.repository.mapper;

import com.github.open.courier.core.transport.SubscribeGroupId;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Mapper
@Repository
public interface SubscribeGroupIdMapper {

    void insert(@Param("id") int id,
                @Param("state") int state,
                @Param("holder") String holder,
                @Param("time") Date time);

    void release(@Param("id") int id, @Param("holder") String holder);

    List<SubscribeGroupId> list();

    void releaseByIds(@Param("ids") Collection<String> ids);

    void deleteByIds(@Param("ids") Collection<Integer> ids);

    String selectIdById(int id);

    String selectId(int id);

    int update(@Param("id") int id,
               @Param("state") int state,
               @Param("holder") String holder,
               @Param("time") Date time);

}
