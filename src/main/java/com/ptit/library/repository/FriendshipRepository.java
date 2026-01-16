package com.ptit.library.repository;

import com.ptit.library.model.Friendship;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Integer> {

	// có tồn tại quan hệ accepted giữa 2 user bất kể chiều?
	@Query("""
			    SELECT COUNT(f) > 0 FROM Friendship f
			    WHERE f.status = 'accepted'
			      AND ((f.userId = :a AND f.friendId = :b) OR (f.userId = :b AND f.friendId = :a))
			""")
	boolean isFriends(@Param("a") String a, @Param("b") String b);

	// lấy 1 bản ghi cặp bất kể chiều (dùng để accept/decline)
	@Query("""
			    SELECT f FROM Friendship f
			    WHERE (f.userId = :a AND f.friendId = :b) OR (f.userId = :b AND f.friendId = :a)
			""")
	Optional<Friendship> findPair(@Param("a") String a, @Param("b") String b);

	// danh sách bạn (accepted) của user
	@Query("""
			    SELECT f FROM Friendship f
			    WHERE f.status = 'accepted'
			      AND (:me = f.userId OR :me = f.friendId)
			""")
	List<Friendship> findAcceptedByUser(@Param("me") String me);

	// lời mời đang chờ user (received)
	@Query("""
			    SELECT f FROM Friendship f
			    WHERE f.status = 'pending'
			      AND f.friendId = :me
			""")
	List<Friendship> findPendingReceived(@Param("me") String me);

	// lời mời mình đã gửi (outgoing)
	@Query("""
			    SELECT f FROM Friendship f
			    WHERE f.status = 'pending'
			      AND f.userId = :me
			""")
	List<Friendship> findPendingSent(@Param("me") String me);

}