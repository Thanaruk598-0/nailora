package com.nailora.repository;

import com.nailora.entity.AddOn;
import com.nailora.entity.ServiceAddOn;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ServiceAddOnRepository extends JpaRepository<ServiceAddOn, Long> {

	/** ดึง Add-on ที่ active สำหรับ service ที่กำหนด */
	@Query("""
			  select a
			  from ServiceAddOn sao
			  join sao.addOn a
			  where sao.active = true
			    and a.active = true
			    and sao.service.id = :serviceId
			  order by a.name asc
			""")
	List<AddOn> findAllActiveForService(@Param("serviceId") Long serviceId);

	/** ดึง Add-on ที่ active ตามชุด id (ไว้ validate/คิดเงิน) */
	@Query("""
			  select a
			  from ServiceAddOn sao
			  join sao.addOn a
			  where sao.active = true
			    and a.active = true
			    and a.id in :ids
			  order by a.name asc
			""")
	List<AddOn> findAllActiveByIds(@Param("ids") List<Long> ids);
}
