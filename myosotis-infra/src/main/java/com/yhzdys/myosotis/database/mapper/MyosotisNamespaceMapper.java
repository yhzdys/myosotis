package com.yhzdys.myosotis.database.mapper;

import com.yhzdys.myosotis.database.object.MyosotisNamespaceDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MyosotisNamespaceMapper {

    int insert(MyosotisNamespaceDO record);

    Long count(@Param("namespaces") List<String> namespaces, @Param("keyword") String keyword);

    List<MyosotisNamespaceDO> list(@Param("namespaces") List<String> namespaces,
                                   @Param("keyword") String keyword,
                                   @Param("offset") Integer offset,
                                   @Param("length") Integer length);

    MyosotisNamespaceDO selectById(Long id);

    MyosotisNamespaceDO selectByNamespace(String namespace);

    int update(MyosotisNamespaceDO record);

    void delete(Long id);
}
