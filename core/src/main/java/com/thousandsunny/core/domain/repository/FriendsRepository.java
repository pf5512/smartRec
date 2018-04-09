package com.thousandsunny.core.domain.repository;

import com.thousandsunny.core.model.Friends;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


/**
 * Created by Administrator on 2016/9/21 0021.
 */
public interface FriendsRepository extends BaseRepository<Friends> {
    Page<Friends> findByOwnerId(Long id, Pageable pageable);

    List<Friends> findByOwnerIdAndFriendDepartmentId(Long ownerId, Long departmentId);

    Page<Friends> findByOwnerIdAndFriendUsernameContaining(Long id, String keyWord, Pageable pageable);

    Page<Friends> findByOwnerIdAndFriendDepartmentIdIn(Long id, List<Long> Ids, Pageable pageable);

    Friends findByOwnerIdAndFriendId(Long ownerId, Long friendId);

    Friends findByOwnerTokenAndFriendToken(String userToken, String checkedUserToken);

    Page<Friends> findByOwnerToken(String userToken, Pageable pageable);

    Page<Friends> findByOwnerTokenAndFriendRealNameContaining(String userToken, String username, Pageable pageable);

    Long countByOwnerToken(String userToken);

    List<Friends> findByOwnerToken(String userToken);

    List<Friends> findByOwnerId(Long id);
}
