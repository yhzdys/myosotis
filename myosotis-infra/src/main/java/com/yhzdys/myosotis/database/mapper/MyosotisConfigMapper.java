package com.yhzdys.myosotis.database.mapper;

import com.yhzdys.myosotis.database.object.MyosotisConfigDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.List;

@Mapper
public interface MyosotisConfigMapper {

    int insert(MyosotisConfigDO record);

    MyosotisConfigDO selectById(Long id);

    MyosotisConfigDO selectByKey(@Param("namespace") String namespace, @Param("configKey") String configKey);

    Long count(@Param("namespaces") List<String> namespaces,
               @Param("namespace") String namespace,
               @Param("keyword") String keyword);

    Long countByNamespace(@Param("namespace") String namespace);

    List<MyosotisConfigDO> list(@Param("namespaces") List<String> namespaces,
                                @Param("namespace") String namespace,
                                @Param("keyword") String keyword,
                                @Param("offset") Integer offset,
                                @Param("length") Integer length);

    int update(MyosotisConfigDO record);

    void delete(Long id);

    List<MyosotisConfigDO> listByNamespace(String namespace);

    List<MyosotisConfigDO> listByIds(@Param("ids") ArrayList<Long> longs);

    List<MyosotisConfigDO> listByKeys(@Param("namespace") String namespace, @Param("configKeys") List<String> configKeys);
}
