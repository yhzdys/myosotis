package com.yhzdys.myosotis.database.mapper;

import com.yhzdys.myosotis.database.object.MyosotisUserDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MyosotisUserMapper {

    int insert(MyosotisUserDO user);

    MyosotisUserDO selectById(Long id);

    MyosotisUserDO selectByUsername(String username);

    int update(MyosotisUserDO record);

    Long count(String keyword);

    List<MyosotisUserDO> list(@Param("keyword") String keyword,
                              @Param("offset") Integer offset,
                              @Param("length") Integer length);

    int delete(Long id);
}
