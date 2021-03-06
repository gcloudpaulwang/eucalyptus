package com.eucalyptus.reporting;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import org.apache.log4j.Logger;

import com.eucalyptus.reporting.event_store.ReportingElasticIpEventStore;
import com.eucalyptus.reporting.event_store.ReportingInstanceEventStore;
import com.eucalyptus.reporting.event_store.ReportingS3ObjectEventStore;
import com.eucalyptus.reporting.event_store.ReportingVolumeEventStore;
import com.eucalyptus.reporting.event_store.ReportingVolumeSnapshotEventStore;
import com.eucalyptus.reporting.domain.ReportingAccountCrud;
import com.eucalyptus.reporting.domain.ReportingUserCrud;
import com.eucalyptus.util.ExposedCommand;

/**
 * <p>FalseDataGenerator generates false data for reporting. It generates fake instance starting
 * and ending times, imaginary volumes, fictitious buckets and objects, non-existent elastic IPs,
 * and fake accounts and users. 
 * 
 * <p>FalseDataGenerator is meant to be called from the <code>CommandServlet</code>
 * 
 * <p>To run: use devel/generate_false_data.sh; NOTE: read docs at top of that shell script
 * 
 * <p>False data should be deleted afterward by calling the <pre>deleteFalseData</pre> method.
 * 
 * <p><i>"False data can come from many sources: academic, social, professional... False data can
 * cause one to make stupid mistakes..."</i>
 *   - L. Ron Hubbard
 */
public class FalseDataGenerator
{
	private static final long START_TIME       = 1104566400000l; //Jan 1, 2005 12:00AM
	private static final long PERIOD_DURATION  = 86400000l; //24 hrs in milliseconds
	private static final int  NUM_PERIODS      = 300;

	private static final int NUM_USERS_PER_ACCOUNT        = 16;
	private static final int NUM_ACCOUNTS_PER_CLUSTER     = 8;
	private static final int NUM_CLUSTERS_PER_ZONE        = 4;
	private static final int NUM_AVAIL_ZONE               = 2;
	
	private static final int NUM_PERIODS_PER_OBJECT       = 30;
	private static final int NUM_VERSIONS_PER_OBJECT      = 3;
	private static final int NUM_PERIODS_PER_BUCKET       = 90;
	private static final int NUM_PERIODS_PER_SNAPSHOT     = 30;
	/* NOTE: In this case "entity" refers to an Instance, Volume, or Elastic IP. There are always
	 *  the same number of instances, volumes, and elastic IPs, because each volume is repeatedly
	 *  attached and detached from its associated instance and similarly with IPs.
	 */
	private static final int NUM_PERIODS_PER_ENTITY       = 30;

	private static final long VOLUME_SIZE   = 2;
	private static final long SNAPSHOT_SIZE = 2;
	private static final long OBJECT_SIZE   = 2;

	private static final long INSTANCE_CUMULATIVE_DISK_USAGE_PER_PERIOD                 = 2;
	private static final long INSTANCE_CUMULATIVE_NET_INCOMING_BETWEEN_USAGE_PER_PERIOD = 3;
	private static final long INSTANCE_CUMULATIVE_NET_INCOMING_WITHIN_PER_PERIOD        = 4;
	private static final long INSTANCE_CUMULATIVE_NET_INCOMING_PUBLIC_PER_PERIOD        = 5;
	private static final long INSTANCE_CUMULATIVE_NET_OUTGOING_BETWEEN_USAGE_PER_PERIOD = 6;
	private static final long INSTANCE_CUMULATIVE_NET_OUTGOING_WITHIN_PER_PERIOD        = 7;
	private static final long INSTANCE_CUMULATIVE_NET_OUTGOING_PUBLIC_PER_PERIOD        = 8;
	private static final int  INSTANCE_CPU_UTILIZATION_PER_PERIOD                       = 10;

	private static final long VOLUME_CUMULATIVE_READ_PER_PERIOD    = 2;
	private static final long VOLUME_CUMULATIVE_WRITTEN_PER_PERIOD = 3;
	
	private static final long OBJECT_CUMULATIVE_READ_PER_PERIOD    = 2;
	private static final long OBJECT_CUMULATIVE_WRITTEN_PER_PERIOD = 3;
	private static final long OBJECT_CUMULATIVE_GETS_PER_PERIOD    = 4;
	private static final long OBJECT_CUMULATIVE_PUTS_PER_PERIOD    = 5;

	private static final int ATTACH_PERIODS_DURATION     = 25;

	
	private static final long INSTANCE_UUID_START    = 1 * (2<<24);
	private static final long VOLUME_UUID_START      = 2 * (2<<24);
	private static final long ELASTIC_IP_UUID_START  = 3 * (2<<24);
	private static final long SNAPSHOT_UUID_START    = 4 * (2<<24);
	private static final long BUCKET_UUID_START      = 5 * (2<<24);
	private static final long OBJECT_UUID_START      = 6 * (2<<24);

	private static Logger log = Logger.getLogger( FalseDataGenerator.class );


	private static final String UUID_FORMAT = "UUID-%d-%d";
	
	private enum FalseInstanceType
	{
		M1SMALL("m1.small"),
		C1MEDIUM("c1.medium"),
		M1LARGE("m1.large"),
		M1XLARGE("m1.xlarge"),
		C1XLARGE("c1.xlarge");
		
		private final String name;
		
		private FalseInstanceType(String name) {
			this.name = name;
		}
		
		public String toString() { return name; }
	}
	
	
	@ExposedCommand
	public static void generateFalseData()
	{
		log.debug(" ----> GENERATING FALSE DATA");
		

		/* Generate every combination of zones, clusters, accounts, and users */
		int uniqueUserId = 0;
		int uniqueAccountId = 0;
		int uniqueClusterId = 0;
		for (int availZoneNum = 0; availZoneNum<NUM_AVAIL_ZONE; availZoneNum++) {
			String availZone = "zone-" + availZoneNum;
			for (int clusterNum=0; clusterNum<NUM_CLUSTERS_PER_ZONE; clusterNum++) {
				uniqueClusterId++;
				String cluster = "cluster-" + uniqueClusterId;
				for (int accountNum=0; accountNum<NUM_ACCOUNTS_PER_CLUSTER; accountNum++) {
					uniqueAccountId++;
					String accountId = "acct-" + uniqueAccountId;
					String accountName = "account-" + uniqueAccountId;
					ReportingAccountCrud.getInstance().createOrUpdateAccount(accountId, accountName);
					for (int userNum=0; userNum<NUM_USERS_PER_ACCOUNT; userNum++) {
						log.debug(String.format("Generating usage for user %d\n", userNum));
						String user = "user-" + userNum;
						uniqueUserId++;
						List<Attachment> attachments = new ArrayList<Attachment>();

						/* For every zone/cluster/account/user combination, generate instances,
						 * volumes, IPs, buckets, and objects. 
						 * Also generate usage.
						 */

						String userId = "u-" + uniqueUserId;
						String userName = "user-" + uniqueUserId;
						ReportingUserCrud.getInstance().createOrUpdateUser(userId, accountId, userName);

						/* These uuids must not overlap as we want the userNum/uuidNum combo to be
						 * unique here. We need to know the full range of every instance, volume,
						 * snapshot, ip, and object uuids generated so far, for each user, in order
						 * to generate usage later. As a result we must have non-overlapping start
						 * values for each of them.
						 */
						long instanceUuidNum   = INSTANCE_UUID_START;
						long volumeUuidNum     = VOLUME_UUID_START;
						long elasticIpUuidNum  = ELASTIC_IP_UUID_START;
						long snapshotUuidNum   = SNAPSHOT_UUID_START;
						long bucketUuidNum     = BUCKET_UUID_START;
						long objectUuidNum     = OBJECT_UUID_START;

						String instanceUuid  = "(none)";
						String volumeUuid    = "(none)";
						String elasticIpUuid = "(none)";
						String bucketName    = "(none)";
						int createdInstanceNum = 0;
						for (int periodNum=0; periodNum<NUM_PERIODS; periodNum++) {
							log.debug(String.format(" Generating usage for period %d\n", periodNum));
							long timeMs = START_TIME + (PERIOD_DURATION*periodNum);

							/* Create a fake instance, a fake volume, and a fake elastic IP if they should be created in this period. */
							if (periodNum % NUM_PERIODS_PER_ENTITY == 0) {
								/* cycle thru instance types */
								int typeNum = createdInstanceNum%FalseInstanceType.values().length;
								FalseInstanceType type = FalseInstanceType.values()[typeNum];
								instanceUuid = String.format(UUID_FORMAT, uniqueUserId, instanceUuidNum++);
								log.debug(String.format("  Generating instance uuid %s\n", instanceUuid));
								ReportingInstanceEventStore.getInstance().insertCreateEvent(instanceUuid,
										("i-" + userNum + "-" + periodNum), timeMs, type.toString(), userId, availZone);
								createdInstanceNum++;

								volumeUuid = String.format(UUID_FORMAT, uniqueUserId, volumeUuidNum++);
								log.debug(String.format("  Generating volume uuid %s\n", volumeUuid));
								ReportingVolumeEventStore.getInstance().insertCreateEvent(volumeUuid, ("vol-" + userNum + "-" + periodNum),
										timeMs, userId, availZone, VOLUME_SIZE);

								elasticIpUuid = String.format(UUID_FORMAT, uniqueUserId, elasticIpUuidNum++);
								log.debug(String.format("  Generating elastic ip uuid %s\n", elasticIpUuid));
								String ip = String.format("%d.%d.%d.%d",
										(userNum >> 8) % 256,
										userNum % 256,
										(periodNum >> 8) % 256,
										periodNum % 256);
								ReportingElasticIpEventStore.getInstance().insertCreateEvent(elasticIpUuid, timeMs, userId, ip);
							}

							/* Create a fake snapshot if one should be created in this period. */
							if (periodNum % NUM_PERIODS_PER_SNAPSHOT == 0) {
								String uuid = String.format(UUID_FORMAT, uniqueUserId, snapshotUuidNum++);
								log.debug(String.format("  Generating snapshot uuid %s\n", uuid));
								ReportingVolumeSnapshotEventStore.getInstance().insertCreateEvent(uuid,
										volumeUuid, ("snap-" + userNum + "-" + periodNum),
										timeMs, userId, SNAPSHOT_SIZE);
							}
							
							/* Create a fake bucket if one should be created in this period. */
							if (periodNum % NUM_PERIODS_PER_BUCKET == 0) {
								bucketName = "bucket-" + bucketUuidNum++;
							}

							/* Create a fake object if one should be created in this period. */
							if (periodNum % NUM_PERIODS_PER_OBJECT == 0) {
								String uuid = String.format(UUID_FORMAT, uniqueUserId, objectUuidNum++);
								log.debug(String.format("  Generating object uuid %s\n", uuid));
								for (int i=0; i<NUM_VERSIONS_PER_OBJECT; i++) {
									ReportingS3ObjectEventStore.getInstance().insertS3ObjectCreateEvent(bucketName, uuid, "0",
										OBJECT_SIZE, timeMs, userId);
								}
							}



							/* Generate instance usage in this period for every instance running from before */
							double oneMB = 1024d*11024d;
							for (long i=INSTANCE_UUID_START; i<instanceUuidNum-2; i++) {
								String uuid = String.format(UUID_FORMAT, uniqueUserId, i);
								log.debug(String.format("  Generating instance usage uuid %s\n", uuid));
								ReportingInstanceEventStore.getInstance().insertUsageEvent(uuid,
										timeMs, "NetworkIn", 0L, "total", oneMB*periodNum);
								ReportingInstanceEventStore.getInstance().insertUsageEvent(uuid,
										timeMs, "NetworkIn", 0L, "internal", oneMB*2*periodNum);
								ReportingInstanceEventStore.getInstance().insertUsageEvent(uuid,
										timeMs, "NetworkOut", 0L, "total", oneMB*3*periodNum);
								ReportingInstanceEventStore.getInstance().insertUsageEvent(uuid,
										timeMs, "NetworkOut", 0L, "internal", oneMB*4*periodNum);
								ReportingInstanceEventStore.getInstance().insertUsageEvent(uuid,
										timeMs, "DiskReadBytes", 0L, "root", oneMB*5*periodNum);
								ReportingInstanceEventStore.getInstance().insertUsageEvent(uuid,
										timeMs, "DiskWriteBytes", 0L, "root", oneMB*6*periodNum);
								ReportingInstanceEventStore.getInstance().insertUsageEvent(uuid,
										timeMs, "DiskReadBytes", 0L, "ephemeral0", oneMB*7*periodNum);
								ReportingInstanceEventStore.getInstance().insertUsageEvent(uuid,
										timeMs, "DiskWriteBytes", 0L, "ephemeral0", oneMB*8*periodNum);
								ReportingInstanceEventStore.getInstance().insertUsageEvent(uuid,
										timeMs, "VolumeTotalReadTime", 0L, "vda", 100000d*periodNum);
								ReportingInstanceEventStore.getInstance().insertUsageEvent(uuid,
										timeMs, "VolumeTotalWriteTime", 0L, "vda", 200000d*periodNum);
								ReportingInstanceEventStore.getInstance().insertUsageEvent(uuid,
										timeMs, "DiskReadOps", 0L, "vda", 100000d*periodNum);
								ReportingInstanceEventStore.getInstance().insertUsageEvent(uuid,
										timeMs, "DiskWriteOps", 0L, "vda", 200000d*periodNum);
								ReportingInstanceEventStore.getInstance().insertUsageEvent(uuid,
										timeMs, "CPUUtilization", 0L, "default", (double)(PERIOD_DURATION/2)*periodNum);
							}

							/* Attach Volumes and Elastic IPs to Instances */
							ReportingVolumeEventStore.getInstance().insertAttachEvent(volumeUuid, instanceUuid, VOLUME_SIZE, timeMs);
							ReportingElasticIpEventStore.getInstance().insertAttachEvent(elasticIpUuid, instanceUuid, timeMs);
							log.debug(String.format("  Attaching volume %s and ip %s to instance %s\n", volumeUuid, elasticIpUuid, instanceUuid));
							attachments.add(new Attachment(instanceUuid, volumeUuid, elasticIpUuid));

							/* Detach old Volumes and Elastic IPs from old Instances */
							if (attachments.size() >= ATTACH_PERIODS_DURATION) {
								Attachment attachment = attachments.remove(0);
								ReportingVolumeEventStore.getInstance().insertDetachEvent(attachment.getVolumeUuid(),
										attachment.getInstanceUuid(), timeMs);
								ReportingElasticIpEventStore.getInstance().insertDetachEvent(attachment.getElasticIpUuid(),
										attachment.getInstanceUuid(), timeMs);
								log.debug(String.format("  Detaching volume %s and ip %s to instance %s\n",
										attachment.getVolumeUuid(), attachment.getElasticIpUuid(), attachment.getInstanceUuid()));
							}
						}
					}
				}
			}
		}
	}

	private static class Attachment
	{
		private final String instanceUuid;
		private final String volumeUuid;
		private final String elasticIpUuid;
				
		public Attachment(String instanceUuid, String volumeUuid,
				String elasticIpUuid)
		{
			this.instanceUuid = instanceUuid;
			this.volumeUuid = volumeUuid;
			this.elasticIpUuid = elasticIpUuid;
		}
		
		public String getInstanceUuid() {
			return instanceUuid;
		}
		
		public String getVolumeUuid() {
			return volumeUuid;
		}
		
		public String getElasticIpUuid() {
			return elasticIpUuid;
		}

	}

	@ExposedCommand
	public static void generateInstanceHtmlReport()
	{
		log.debug(" ----> GENERATING INSTANCE HTML REPORT");

		Period period = new Period(START_TIME + (PERIOD_DURATION*3), START_TIME + (PERIOD_DURATION * 200));
		
		File file = new File("/tmp/report_instance.html");
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			ReportGenerator.getInstance().generateReport(period, ReportFormat.HTML,
					ReportType.INSTANCE, null, fos );
		} catch (IOException iox) {
			log.error("Error generating report", iox);
		} finally {
			if (fos!=null) {
				try {
					fos.close();
				} catch (IOException e) {
					log.error("Error closing stream", e);
				}
			}
		}
	}

	@ExposedCommand
	public static void generateInstanceCsvReport()
	{
		log.debug(" ----> GENERATING INSTANCE CSV REPORT");

		Period period = new Period(START_TIME + (PERIOD_DURATION*3), START_TIME + (PERIOD_DURATION * 200));
		
		File file = new File("/tmp/report_instance.csv");
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			ReportGenerator.getInstance().generateReport(period, ReportFormat.CSV,
					ReportType.INSTANCE, null, fos);
		} catch (IOException iox) {
			log.error("Error generating report", iox);
		} finally {
			if (fos!=null) {
				try {
					fos.close();
				} catch (IOException e) {
					log.error("Error closing stream", e);
				}
			}
		}
	}

	@ExposedCommand
	public static void generateVolumeHtmlReport()
	{
		log.debug(" ----> GENERATING VOLUME HTML REPORT");

		Period period = new Period(START_TIME + (PERIOD_DURATION*3), START_TIME + (PERIOD_DURATION * 200));
		
		File file = new File("/tmp/report_volume.html");
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			ReportGenerator.getInstance().generateReport(period, ReportFormat.HTML,
					ReportType.VOLUME, null, fos);
		} catch (IOException iox) {
			log.error("Error generating report", iox);
		} finally {
			if (fos!=null) {
				try {
					fos.close();
				} catch (IOException e) {
					log.error("Error closing stream", e);
				}
			}
		}
	}

	@ExposedCommand
	public static void generateS3HtmlReport()
	{
		log.debug(" ----> GENERATING S3 HTML REPORT");

		Period period = new Period(START_TIME + (PERIOD_DURATION*3), START_TIME + (PERIOD_DURATION * 200));
		
		File file = new File("/tmp/report_s3.html");
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			ReportGenerator.getInstance().generateReport(period, ReportFormat.HTML,
					ReportType.S3, null, fos);
		} catch (IOException iox) {
			log.error("Error generating report", iox);
		} finally {
			if (fos!=null) {
				try {
					fos.close();
				} catch (IOException e) {
					log.error("Error closing stream", e);
				}
			}
		}
	}

	@ExposedCommand
	public static void generateSnapshotHtmlReport()
	{
		log.debug(" ----> GENERATING Snapshot HTML REPORT");

		Period period = new Period(START_TIME + (PERIOD_DURATION*3), START_TIME + (PERIOD_DURATION * 200));
		
		File file = new File("/tmp/report_snapshot.html");
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			ReportGenerator.getInstance().generateReport(period, ReportFormat.HTML,
					ReportType.SNAPSHOT, null, fos);
		} catch (IOException iox) {
			log.error("Error generating report", iox);
		} finally {
			if (fos!=null) {
				try {
					fos.close();
				} catch (IOException e) {
					log.error("Error closing stream", e);
				}
			}
		}
	}

	@ExposedCommand
	public static void generateElasticIpHtmlReport()
	{
		log.debug(" ----> GENERATING ELASTIC IP HTML REPORT");

		Period period = new Period(START_TIME + (PERIOD_DURATION*3), START_TIME + (PERIOD_DURATION * 200));
		
		File file = new File("/tmp/report_elastic_ip.html");
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			ReportGenerator.getInstance().generateReport(period, ReportFormat.HTML,
					ReportType.ELASTIC_IP, null, fos);
		} catch (IOException iox) {
			log.error("Error generating report", iox);
		} finally {
			if (fos!=null) {
				try {
					fos.close();
				} catch (IOException e) {
					log.error("Error closing stream", e);
				}
			}
		}
	}

	@ExposedCommand
	public static void removeFalseData()
	{
		log.debug(" ----> REMOVING FALSE DATA");
	}


	@ExposedCommand
	public static void removeAllData()
	{
		log.debug(" ----> REMOVING ALL DATA");
	}

	public static void printFalseData()
	{
//		InstanceUsageLog usageLog = InstanceUsageLog.getInstanceUsageLog();
//		log.debug(" ----> PRINTING FALSE DATA");
//		for (InstanceUsageLog.LogScanResult result: usageLog.scanLog(new Period(0L, MAX_MS))) {
//
//			InstanceAttributes insAttrs = result.getInstanceAttributes();
//			Period period = result.getPeriod();
//			InstanceUsageData usageData = result.getUsageData();
//
//			log.debug(String.format("instance:%s type:%s user:%s account:%s cluster:%s"
//					+ " zone:%s period:%d-%d netIo:%d diskIo:%d\n",
//					insAttrs.getInstanceId(), insAttrs.getInstanceType(),
//					insAttrs.getUserId(), insAttrs.getAccountId(),
//					insAttrs.getClusterName(), insAttrs.getAvailabilityZone(),
//					period.getBeginningMs(), period.getEndingMs(),
//					usageData.getNetworkIoMegs(), usageData.getDiskIoMegs());
//		}
	}


}
