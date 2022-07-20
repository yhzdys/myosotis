package com.yhzdys.myosotis.database.mapper;

import com.yhzdys.myosotis.database.object.MyosotisAuthorityDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

@Mapper
public interface MyosotisAuthorityMapper {

    int insert(MyosotisAuthorityDO record);

    int batchInsert(@Param("list") List<MyosotisAuthorityDO> list);

    List<MyosotisAuthorityDO> listByNamespace(String namespace);

    List<MyosotisAuthorityDO> listByUsername(String username);

    int deleteByUsername(String username);

    int deleteByNamespace(String namespace);

    int deleteByNamespaces(@Param("username") String username, @Param("namespaces") Collection<String> namespaces);

    int deleteByUsernames(@Param("namespace") String namespace, @Param("usernames") Collection<String> usernames);
}
