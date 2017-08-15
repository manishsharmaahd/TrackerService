package com.ffdc;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import com.ffdc.DataAccesObjects.DataCompressionLockDao;
import com.ffdc.models.DataCompressionLock;

/**
 * 
 * Create Indexes and insert initialization entries for DataCompressionLock table
 * @author LENOVO
 *
 */
public class InitDB {
	private static String DataCompressionLockRow15 = "insert into DataCompressionLock (Type,TimeStamp,InvocatinInstanceId ,AcquireTimeStamp ) values (900000,0,'Available',0)";
	private static String DataCompressionLockRow60 = "insert into DataCompressionLock (Type,TimeStamp,InvocatinInstanceId ,AcquireTimeStamp ) values (3600000,0,'Available',0)";

	public static void intialize() {
		EntityManager em = EntityManagerWrapper.getEntityManager();
		em.getTransaction().begin();
		createIndexes(em);
		Query q;
		q = em.createNativeQuery(DataCompressionLockRow15);
		if (q.executeUpdate() != 1)

			throw new Error("Failed to insert " + DataCompressionLockRow15);

		q = em.createNativeQuery(DataCompressionLockRow60);
		if (q.executeUpdate() != 1)
			throw new Error("Failed to insert " + DataCompressionLockRow60);
		em.getTransaction().commit();
		DataCompressionLockDao d = new DataCompressionLockDao();
		List<DataCompressionLock> l = d.findAll();
		l.forEach(a -> System.out.println(a));
		EntityManagerWrapper.closeEntityManager();
	}

	public static void createIndexes(EntityManager em) {
		String ind1 = "CREATE INDEX  campaincodeEmail  ON hitlog(CampaignCode ,email)";
		String ind2 = "CREATE INDEX  campaincodeFingerPrintNew  ON hitlog(CampaignCode , BrowserFingerPrint)";
		String ind3 = "CREATE INDEX  ServerTS ON hitlog(serverTS)";
		String ind4 = "CREATE INDEX  HitTypeIndex ON hitlog(isOpenHit)";
		String ind5 = "CREATE INDEX  ProcessedIndex ON hitlog(Processed)";
		Query q;
		q = em.createNativeQuery(ind1);
		q.executeUpdate();
		q = em.createNativeQuery(ind2);
		q.executeUpdate();
		q = em.createNativeQuery(ind3);
		q.executeUpdate();
		q = em.createNativeQuery(ind4);
		q.executeUpdate();
		q = em.createNativeQuery(ind5);
		q.executeUpdate();
	
	}
}
