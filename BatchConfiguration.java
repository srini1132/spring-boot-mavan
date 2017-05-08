package com.wba.horizon.batch.modules.kpi.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.batch.runtime.BatchStatus;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;

import com.wba.horizon.batch.modules.kpi.ftp.FtpFileSynchronizer;
import com.wba.horizon.batch.modules.kpi.ftp.KpiFileNames;
import com.wba.horizon.batch.modules.kpi.util.ArrayUtils;
import com.wba.horizon.batch.modules.kpi.util.BatchConstants;
import com.wba.horizon.batch.modules.kpi.util.EmailSender;
import com.wba.horizon.batch.modules.kpi.util.FileUtils;

/**
 * @author srinivasan_m
 *
 */

@Configuration
@EnableConfigurationProperties
@EnableBatchProcessing
@Qualifier("batchConfiguration")
@EnableRetry
public class BatchConfiguration {

	private static final Logger log = LoggerFactory.getLogger(BatchConfiguration.class);

	@Autowired
	private SimpleJobLauncher jobLauncher;

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private JobRepository jobRepository;

	private JobExecution execution;

	@Autowired
	@Qualifier("ftpFileSynchronizer")
	private FtpFileSynchronizer ftpFileSynchronizer;

	@Value("${horizon.storesFlatFileSequance}")
	public String[] storesFlatFileSequance;

	@Value("${horizon.delimiter}")
	public String delimiter;

	@Value("${horizon.maximumItemCount}")
	public int maximumItemCount;

	@Value("${horizon.ftp.destination.filePath}")
	public String localFilePath;
	
	@Value("${horizon.errorFile}")
	public String errorFile;

	@Value("${horizon.ftp.source.filePath}")
	private String remoteFilePath;

	public static Map<String, JobParameters> jobParams = new HashMap<>();

	public static Map<String, String[]> processedFiles = new HashMap<>();
	
	public static Map<String, String> invalidFiles = new HashMap<>();

	@Bean
	public FtpFileSynchronizer ftpFileSynchronizer() {
		return new FtpFileSynchronizer();
	}

	public static boolean IS_STORE_JOB_COMPLETED = true;

	@Bean
	public StoresBatchConfiguration storesBatchConfiguration() {
		return new StoresBatchConfiguration();
	}

	@Bean
	public RxCountBatchConfiguration rxCountBatchConfiguration() {
		return new RxCountBatchConfiguration();
	}

	@Bean
	public RpSalesBatchConfiguration rpSalesBatchConfiguration() {
		return new RpSalesBatchConfiguration();
	}

	@Bean
	public ImproveScRxCountBatchConfiguration improveScRxCountBatchConfiguration() {
		return new ImproveScRxCountBatchConfiguration();
	}

	@Bean
	public ImproveScRpSalesBatchConfiguration improveScRpSalesBatchConfiguration() {
		return new ImproveScRpSalesBatchConfiguration();
	}

	@Bean
	public RegionSalesBatchConfiguration regionSalesBatchConfiguration() {
		return new RegionSalesBatchConfiguration();
	}

	@Bean
	public OperationsSalesConfiguration operationsSalesConfiguration() {
		return new OperationsSalesConfiguration();
	}

	@Bean
	public DistrictSalesConfiguration districtSalesConfiguration() {
		return new DistrictSalesConfiguration();
	}

	@Bean
	public StoreFeedConfiguration storeFeedConfiguration() {
		return new StoreFeedConfiguration();
	}

	@Bean
	public PayrollBatchConfiguration payrollBatchConfiguration() {
		return new PayrollBatchConfiguration();
	}

	@Bean
	public ChainFeedBatchConfiguration chainFeedBatchConfiguration() {
		return new ChainFeedBatchConfiguration();
	}

	@Bean
	public AreaBatchConfiguration areaBatchConfiguration() {
		return new AreaBatchConfiguration();
	}

	@Bean
	public DistrictPayrollBatchConfiguration districtPayrollBatchConfiguration() {
		return new DistrictPayrollBatchConfiguration();
	}

	@Bean
	public ChainPayrollBatchConfiguration chainPayrollBatchConfiguration() {
		return new ChainPayrollBatchConfiguration();
	}

	@Bean
	public AreaPayrollBatchConfiguration areaPayrollBatchConfiguration() {
		return new AreaPayrollBatchConfiguration();
	}

	@Bean
	public OperationPayrollBatchConfiguration operationPayrollBatchConfiguration() {
		return new OperationPayrollBatchConfiguration();
	}

	@Bean
	public RegionPayrollBatchConfiguration regionPayrollBatchConfiguration() {
		return new RegionPayrollBatchConfiguration();
	}

	@Bean
	public StorePayrollBatchConfiguration storePayrollBatchConfiguration() {
		return new StorePayrollBatchConfiguration();
	}

	@Bean
	public MonthlyRpSalesBatchConfiguration monthlyRpSalesBatchConfiguration() {

		return new MonthlyRpSalesBatchConfiguration();
	}

	@Bean
	public MonthlyRxCountBatchConfiguration monthlyRxCountBatchConfiguration() {

		return new MonthlyRxCountBatchConfiguration();
	}

	@Bean
	public PosSalesBatchConfiguration posSalesBatchConfiguration() {

		return new PosSalesBatchConfiguration();
	}
	

	@Bean
	public DailyBeautySalesConfiguration dailyBeautySalesConfiguration() {

		return new DailyBeautySalesConfiguration();
	}

	@Bean
	public FileUtils fileUtils() {
		return new FileUtils();
	}

	@Retryable(maxAttempts = 2, backoff = @Backoff(delay = 1500), include = { RuntimeException.class })
	public void perform() throws Exception {

		invalidFileNameLogging();
		
		
		launchStoreBatchJob();

		if (IS_STORE_JOB_COMPLETED
				&& ArrayUtils.isEmpty(fileUtils().getFiles(localFilePath, KpiFileNames.HRZ_STORE_INFO.name()))) {

			jobLauncher.setTaskExecutor(taskExecutor());

			jobLauncher.setJobRepository(jobRepository);

			launchRpSalesBatchJob();

			launchRxCountBatchJob();

			launchMonthlyRpSales();

			launchMonthlyRxCountBatchJob();

			launchDailyBeautySalesBatchJob();

			launchStoreSalesBatchJob();

			launchOperationSalesBatchJob();

			launchDistrictSales();

			launchRegionSalesBatchJob();

			launchChainSalesBatchJob();

			launchAreaSalesBatchJob();

			launchPayrollBatchJob();

			launchDistrictPayrollBatchJob();

			launchChainPayrollBatchJob();

			launchAreaPayrollBatchJob();

			launchOperationPayrollJob();

			launchRegionPayrollBatchJob();

			launchStorePayrollBatchJob();
			
			launchPosSalesJob();
		}

		log.info("BatchConfiguration.perform() : END");
	}

	public void launchPosSalesJob() throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		
		JobParameters posSalesJobParam = new JobParametersBuilder()
				.addString(BatchConstants.PARAM_POS_BATCH, String.valueOf(System.currentTimeMillis()))
				.toJobParameters();
		execution = jobLauncher.run(posSalesBatchConfiguration().importPosSalesJob(),posSalesJobParam);
		
	}

	public void launchStorePayrollBatchJob() {

		try {

			File[] storePayrollFiles = fileUtils().getFiles(localFilePath,
					KpiFileNames.HRZ_MONTHLY_STORE_PAYROLL.name());

			if (!ArrayUtils.isEmpty(storePayrollFiles)) {

				log.info("importStorePayrollJob finished with status : STARTED");

				boolean isJobRunning = isJobRunning(BatchConstants.NAME_STORE_PAYROLL);

				if (!isJobRunning) {

					JobParameters storePayrollJobParam = new JobParametersBuilder()
							.addString(BatchConstants.PARAM_STORE_PAYROLL, String.valueOf(System.currentTimeMillis()))
							.toJobParameters();

					execution = jobLauncher.run(storePayrollBatchConfiguration().importStorePayrollJob(),
							storePayrollJobParam);

					jobParams.put(BatchConstants.NAME_STORE_PAYROLL, execution.getJobParameters());

				}

				removeProcessedFile(storePayrollFiles, execution.getExitStatus().getExitCode());

				log.info("importStorePayrollJob finished with status : " + execution.getStatus());
			}

		} catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
				| JobParametersInvalidException e) {

			handleException(e, "Exception occures while processing " + execution.getJobConfigurationName());
		}
	}

	public void launchRegionPayrollBatchJob() {

		try {

			File[] regionPayrollFiles = fileUtils().getFiles(localFilePath,
					KpiFileNames.HRZ_MONTHLY_REGION_PAYROLL.name());

			if (!ArrayUtils.isEmpty(regionPayrollFiles)) {

				log.info("importRegionPayrollJob finished with status : STARTED");

				boolean isJobRunning = isJobRunning(BatchConstants.NAME_REGION_PAYROLL);

				if (!isJobRunning) {

					JobParameters regionPayrollJobParam = new JobParametersBuilder()
							.addString(BatchConstants.PARAM_REGION_PAYROLL, String.valueOf(System.currentTimeMillis()))
							.toJobParameters();

					execution = jobLauncher.run(regionPayrollBatchConfiguration().importRegionPayrollJob(),
							regionPayrollJobParam);

					jobParams.put(BatchConstants.NAME_REGION_PAYROLL, execution.getJobParameters());

				}

				removeProcessedFile(regionPayrollFiles, execution.getExitStatus().getExitCode());

				log.info("importRegionPayrollJob finished with status : " + execution.getStatus());
			}

		} catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
				| JobParametersInvalidException e) {

			handleException(e, "Exception occures while processing " + execution.getJobConfigurationName());
		}
	}

	public void launchOperationPayrollJob() {

		try {

			File[] operationPayrollFiles = fileUtils().getFiles(localFilePath,
					KpiFileNames.HRZ_MONTHLY_OP_PAYROLL.name());

			if (!ArrayUtils.isEmpty(operationPayrollFiles)) {

				log.info("importOperationPayrollJob finished with status : STARTED");

				boolean isJobRunning = isJobRunning(BatchConstants.NAME_OP_PAYROLL);

				if (!isJobRunning) {

					JobParameters operationPayrollJobParam = new JobParametersBuilder()
							.addString(BatchConstants.PARAM_OP_PAYROLL, String.valueOf(System.currentTimeMillis()))
							.toJobParameters();

					execution = jobLauncher.run(operationPayrollBatchConfiguration().importOperationPayrollJob(),
							operationPayrollJobParam);

					jobParams.put(BatchConstants.NAME_OP_PAYROLL, execution.getJobParameters());

				}

				removeProcessedFile(operationPayrollFiles, execution.getExitStatus().getExitCode());

				log.info("importOperationPayrollJob finished with status : " + execution.getStatus());
			}

		} catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
				| JobParametersInvalidException e) {

			handleException(e, "Exception occures while processing " + execution.getJobConfigurationName());
		}
	}

	public void launchAreaPayrollBatchJob() {

		try {

			File[] areaPayrollFiles = fileUtils().getFiles(localFilePath, KpiFileNames.HRZ_MONTHLY_AREA_PAYROLL.name());

			if (!ArrayUtils.isEmpty(areaPayrollFiles)) {

				log.info("importAreaPayrollJob finished with status : STARTED");

				boolean isJobRunning = isJobRunning(BatchConstants.NAME_AREA_PAYROLL);

				if (!isJobRunning) {

					JobParameters areaPayrollJobParam = new JobParametersBuilder()
							.addString(BatchConstants.PARAM_AREA_PAYROLL, String.valueOf(System.currentTimeMillis()))
							.toJobParameters();

					execution = jobLauncher.run(areaPayrollBatchConfiguration().importAreaPayrollJob(),
							areaPayrollJobParam);

					jobParams.put(BatchConstants.NAME_AREA_PAYROLL, execution.getJobParameters());

				}

				removeProcessedFile(areaPayrollFiles, execution.getExitStatus().getExitCode());

				log.info("importAreaPayrollJob finished with status : " + execution.getStatus());
			}

		} catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
				| JobParametersInvalidException e) {

			handleException(e, "Exception occures while processing " + execution.getJobConfigurationName());
		}
	}

	public void launchChainPayrollBatchJob() {

		try {

			File[] chainPayrollFiles = fileUtils().getFiles(localFilePath,
					KpiFileNames.HRZ_MONTHLY_CHAIN_PAYROLL.name());

			if (!ArrayUtils.isEmpty(chainPayrollFiles)) {

				log.info("importChainPayrollJob finished with status : STARTED");

				boolean isJobRunning = isJobRunning(BatchConstants.NAME_CHAIN_PAYROLL);

				if (!isJobRunning) {

					JobParameters chainPayrollJobParam = new JobParametersBuilder()
							.addString(BatchConstants.PARAM_CHAIN_PAYROLL, String.valueOf(System.currentTimeMillis()))
							.toJobParameters();

					execution = jobLauncher.run(chainPayrollBatchConfiguration().importChainPayrollJob(),
							chainPayrollJobParam);

					jobParams.put(BatchConstants.NAME_CHAIN_PAYROLL, execution.getJobParameters());

				}

				removeProcessedFile(chainPayrollFiles, execution.getExitStatus().getExitCode());

				log.info("importChainPayrollJob finished with status : " + execution.getStatus());
			}

		} catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
				| JobParametersInvalidException e) {

			handleException(e, "Exception occures while processing " + execution.getJobConfigurationName());
		}

	}

	public void launchDistrictPayrollBatchJob() {

		try {

			File[] districtPayrollFiles = fileUtils().getFiles(localFilePath,
					KpiFileNames.HRZ_MONTHLY_DISTRICT_PAYROLL.name());

			if (!ArrayUtils.isEmpty(districtPayrollFiles)) {

				log.info("importDistrictPayrollJob finished with status : STARTED");

				boolean isJobRunning = isJobRunning(BatchConstants.NAME_DISTRICT_PAYROLL);

				if (!isJobRunning) {

					JobParameters districtPayrollJobParam = new JobParametersBuilder()
							.addString(BatchConstants.PARAM_DISTRICT_PAYROLL,
									String.valueOf(System.currentTimeMillis()))
							.toJobParameters();

					execution = jobLauncher.run(districtPayrollBatchConfiguration().importDistrictPayrollJob(),
							districtPayrollJobParam);

					jobParams.put(BatchConstants.NAME_DISTRICT_PAYROLL, execution.getJobParameters());

				}

				removeProcessedFile(districtPayrollFiles, execution.getExitStatus().getExitCode());

				log.info("importDistrictPayrollJob finished with status : " + execution.getStatus());
			}

		} catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
				| JobParametersInvalidException e) {

			handleException(e, "Exception occures while processing " + execution.getJobConfigurationName());
		}
	}

	public void launchPayrollBatchJob() {

		try {

			File[] strPayrollFiles = fileUtils().getFiles(localFilePath, KpiFileNames.HRZ_MONTHLY_STR_PAYROLL.name());

			if (!ArrayUtils.isEmpty(strPayrollFiles)) {

				log.info("importPayrollJob finished with status : STARTED");

				boolean isJobRunning = isJobRunning(BatchConstants.NAME_PAYROLL);

				if (!isJobRunning) {

					JobParameters payrollJobParam = new JobParametersBuilder()
							.addString(BatchConstants.PARAM_PAYROLL, String.valueOf(System.currentTimeMillis()))
							.toJobParameters();

					execution = jobLauncher.run(payrollBatchConfiguration().importPayrollJob(), payrollJobParam);

					jobParams.put(BatchConstants.NAME_PAYROLL, execution.getJobParameters());

				}

				removeProcessedFile(strPayrollFiles, execution.getExitStatus().getExitCode());

				log.info("importPayrollJob finished with status : " + execution.getStatus());
			}

		} catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
				| JobParametersInvalidException e) {

			handleException(e, "Exception occures while processing " + execution.getJobConfigurationName());
		}
	}

	public void launchAreaSalesBatchJob() {

		try {

			File[] areaSalesFiles = fileUtils().getFiles(localFilePath, KpiFileNames.HRZ_MONTHLY_AREA_IMPROVE.name());

			if (!ArrayUtils.isEmpty(areaSalesFiles)) {

				log.info("importAreaJob finished with status : STARTED");

				boolean isJobRunning = isJobRunning(BatchConstants.NAME_AREA_SALES);

				if (!isJobRunning) {

					JobParameters areaJobParam = new JobParametersBuilder()
							.addString(BatchConstants.PARAM_AREA_SALES, String.valueOf(System.currentTimeMillis()))
							.toJobParameters();

					execution = jobLauncher.run(areaBatchConfiguration().importAreaJob(), areaJobParam);

					jobParams.put(BatchConstants.NAME_AREA_SALES, execution.getJobParameters());

				}

				removeProcessedFile(areaSalesFiles, execution.getExitStatus().getExitCode());

				log.info("importAreaJob finished with status : " + execution.getStatus());
			}

		} catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
				| JobParametersInvalidException e) {

			handleException(e, "Exception occures while processing " + execution.getJobConfigurationName());
		}
	}

	public void launchChainSalesBatchJob() {

		try {

			File[] chainSalesFiles = fileUtils().getFiles(localFilePath, KpiFileNames.HRZ_MONTHLY_CHAIN_IMPROVE.name());

			if (!ArrayUtils.isEmpty(chainSalesFiles)) {

				log.info("importRegionSalesJob finished with status : STARTED");

				boolean isJobRunning = isJobRunning(BatchConstants.NAME_CHAIN_SALES);

				if (!isJobRunning) {

					JobParameters chainFeedJobParam = new JobParametersBuilder()
							.addString(BatchConstants.PARAM_CHAIN_SALES, String.valueOf(System.currentTimeMillis()))
							.toJobParameters();

					execution = jobLauncher.run(chainFeedBatchConfiguration().importChainFeedJob(), chainFeedJobParam);

					jobParams.put(BatchConstants.NAME_CHAIN_SALES, execution.getJobParameters());

				}

				removeProcessedFile(chainSalesFiles, execution.getExitStatus().getExitCode());

				log.info("importRegionSalesJob finished with status : " + execution.getStatus());
			}

		} catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
				| JobParametersInvalidException e) {

			handleException(e, "Exception occures while processing " + execution.getJobConfigurationName());
		}
	}

	public void launchRegionSalesBatchJob() {

		try {

			File[] regionSalesFiles = fileUtils().getFiles(localFilePath,
					KpiFileNames.HRZ_MONTHLY_REGION_IMPROVE.name());

			if (!ArrayUtils.isEmpty(regionSalesFiles)) {

				boolean isJobRunning = isJobRunning(BatchConstants.NAME_REGION_SALES);

				if (!isJobRunning) {

					log.info("importRegionSalesJob finished with status : STARTED");

					JobParameters regionSalesJobParam = new JobParametersBuilder()
							.addString(BatchConstants.PARAM_REGION_SALES, String.valueOf(System.currentTimeMillis()))
							.toJobParameters();

					execution = jobLauncher.run(regionSalesBatchConfiguration().importRegionSalesJob(),
							regionSalesJobParam);

					jobParams.put(BatchConstants.NAME_REGION_SALES, execution.getJobParameters());

				}

				removeProcessedFile(regionSalesFiles, execution.getExitStatus().getExitCode());

				log.info("importRegionSalesJob finished with status : " + execution.getStatus());
			}

		} catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
				| JobParametersInvalidException e) {

			handleException(e, "Exception occures while processing " + execution.getJobConfigurationName());
		}
	}

	public void launchOperationSalesBatchJob() {
		File[] operationSalesFiles = fileUtils().getFiles(localFilePath, KpiFileNames.HRZ_MONTHLY_OP_IMPROVE.name());

		try {

			if (!ArrayUtils.isEmpty(operationSalesFiles)) {

				log.info("importOperationFeedJob finished with status : STARTED");

				boolean isJobRunning = isJobRunning(BatchConstants.NAME_OP_SALES);

				if (!isJobRunning) {

					JobParameters operationSalesJobParam = new JobParametersBuilder()
							.addString(BatchConstants.PARAM_OP_SALES, String.valueOf(System.currentTimeMillis()))
							.toJobParameters();

					execution = jobLauncher.run(operationsSalesConfiguration().importOperationFeedJob(),
							operationSalesJobParam);

					jobParams.put(BatchConstants.NAME_OP_SALES, execution.getJobParameters());

				}

				removeProcessedFile(operationSalesFiles, execution.getExitStatus().getExitCode());

				log.info("importOperationFeedJob finished with status : " + execution.getStatus());
			}

		} catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
				| JobParametersInvalidException e) {

			handleException(e, "Exception occures while processing " + execution.getJobConfigurationName());
		}
	}

	public void launchStoreSalesBatchJob() {

		try {

			File[] storeSalesFiles = fileUtils().getFiles(localFilePath, KpiFileNames.HRZ_MONTHLY_STORE_IMPROVE.name());

			if (!ArrayUtils.isEmpty(storeSalesFiles)) {

				log.info("importDistrictSalesJob finished with status : STARTED");

				boolean isJobRunning = isJobRunning(BatchConstants.NAME_STORE_SALES);

				if (!isJobRunning) {

					JobParameters storeFeedJobParam = new JobParametersBuilder()
							.addString(BatchConstants.PARAM_STORE_SALES, String.valueOf(System.currentTimeMillis()))
							.toJobParameters();

					execution = jobLauncher.run(storeFeedConfiguration().importStoreFeedJob(), storeFeedJobParam);

					jobParams.put(BatchConstants.NAME_STORE_SALES, execution.getJobParameters());

				}

				removeProcessedFile(storeSalesFiles, execution.getExitStatus().getExitCode());

				log.info("importStoreFeedJob finished with status : " + execution.getStatus());
			}

		} catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
				| JobParametersInvalidException e) {

			handleException(e, "Exception occures while processing " + execution.getJobConfigurationName());
		}
	}

	public void launchDailyBeautySalesBatchJob() {

		try {

			File[] dailyBeautySalesFiles = fileUtils().getFiles(localFilePath, KpiFileNames.HRZ_DAILY_BEAUTY.name());

			if (!ArrayUtils.isEmpty(dailyBeautySalesFiles)) {

				log.info("importDailyBeautySalesJob finished with status : STARTED");

				boolean isJobRunning = isJobRunning(BatchConstants.NAME_DAILY_BEAUTY_SALES);

				if (!isJobRunning) {

					JobParameters dailyBeautySalesJobParam = new JobParametersBuilder()
							.addString(BatchConstants.PARAM_DAILY_BEAUTY_SALES,
									String.valueOf(System.currentTimeMillis()))
							.toJobParameters();

					execution = jobLauncher.run(dailyBeautySalesConfiguration().importDailyBeautySalesJob(),
							dailyBeautySalesJobParam);

					jobParams.put(BatchConstants.NAME_DAILY_BEAUTY_SALES, execution.getJobParameters());

				}

				removeProcessedFile(dailyBeautySalesFiles, execution.getExitStatus().getExitCode());

				log.info("importDailyBeautySalesJob finished with status : " + execution.getStatus());
			}

		} catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
				| JobParametersInvalidException e) {

			handleException(e, "Exception occures while processing " + execution.getJobConfigurationName());
		}
	}

	public void launchMonthlyRxCountBatchJob() {

		try {
			File[] monthlyRxCountFiles = fileUtils().getFiles(localFilePath, KpiFileNames.HRZ_MONTHLY_RXCOUNT.name());

			if (!ArrayUtils.isEmpty(monthlyRxCountFiles)) {

				boolean isJobRunning = isJobRunning(BatchConstants.NAME_MONTHLY_RX_COUNT);

				if (!isJobRunning) {

					log.info("importMonthlyRxCountJob finished with status : STARTED");

					JobParameters monthlyRxCountJobParam = new JobParametersBuilder()
							.addString(BatchConstants.PARAM_MONTHLY_RX_COUNT,
									String.valueOf(System.currentTimeMillis()))
							.toJobParameters();

					execution = jobLauncher.run(monthlyRxCountBatchConfiguration().importMonthlyRxCountJob(),
							monthlyRxCountJobParam);

					jobParams.put(BatchConstants.NAME_MONTHLY_RX_COUNT, execution.getJobParameters());

				}

				removeProcessedFile(monthlyRxCountFiles, execution.getExitStatus().getExitCode());

				log.info("importMonthlyRxCountJob finished with status : " + execution.getStatus());
			}
		} catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
				| JobParametersInvalidException e) {

			handleException(e, "Exception occures while processing " + execution.getJobConfigurationName());
		}
	}

	public void launchMonthlyRpSales() {

		try {

			File[] monthlyRpSalesFiles = fileUtils().getFiles(localFilePath, KpiFileNames.HRZ_MONTHLY_RPSALES.name());

			if (!ArrayUtils.isEmpty(monthlyRpSalesFiles)) {

				boolean isJobRunning = isJobRunning(BatchConstants.NAME_MONTHLY_RP_SALES);

				if (!isJobRunning) {

					log.info("importMonthlyRpSalesJob finished with status : STARTED");

					JobParameters monthlyRpSalesJobParam = new JobParametersBuilder()
							.addString(BatchConstants.PARAM_MONTHLY_RP_SALES,
									String.valueOf(System.currentTimeMillis()))
							.toJobParameters();

					execution = jobLauncher.run(monthlyRpSalesBatchConfiguration().importMonthlyRpSalesJob(),
							monthlyRpSalesJobParam);

					jobParams.put(BatchConstants.NAME_MONTHLY_RP_SALES, execution.getJobParameters());

				}

				removeProcessedFile(monthlyRpSalesFiles, execution.getExitStatus().getExitCode());

				log.info("importMonthlyRpSalesJob finished with status : " + execution.getStatus());
			}

		} catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
				| JobParametersInvalidException e) {

			handleException(e, "Exception occures while processing " + execution.getJobConfigurationName());
		}
	}

	public void launchRxCountBatchJob() {

		try {

			File[] rxCountFiles = fileUtils().getFiles(localFilePath, KpiFileNames.HRZ_DAILY_RXCOUNT.name());

			if (!ArrayUtils.isEmpty(rxCountFiles)) {

				log.info("importRxCountJob finished with status : STARTED");

				boolean isJobRunning = isJobRunning(BatchConstants.NAME_RX_COUNT);

				if (!isJobRunning) {

					JobParameters rxJobParam = new JobParametersBuilder()
							.addString(BatchConstants.PARAM_RX_COUNT, String.valueOf(System.currentTimeMillis()))
							.toJobParameters();

					execution = jobLauncher.run(rxCountBatchConfiguration().importRxCountJob(), rxJobParam);

					execution.getJobInstance().incrementVersion();

					jobParams.put(BatchConstants.NAME_RX_COUNT, execution.getJobParameters());

				}

				removeProcessedFile(rxCountFiles, execution.getExitStatus().getExitCode());

				log.info("importRxCountJob finished with status : " + execution.getStatus());
			}

		} catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
				| JobParametersInvalidException e) {

			handleException(e, "Exception occures while processing " + execution.getJobConfigurationName());
		}
	}

	public void launchRpSalesBatchJob() {

		try {

			File[] rpSalesFiles = fileUtils().getFiles(localFilePath, KpiFileNames.HRZ_DAILY_RPSALES.name());

			if (!ArrayUtils.isEmpty(rpSalesFiles)) {

				log.info("importRpSalesJob  : STARTED");

				boolean isJobRunning = isJobRunning(BatchConstants.NAME_RP_SALES);

				if (!isJobRunning) {

					JobParameters rpJobParam = new JobParametersBuilder()
							.addString(BatchConstants.PARAM_RP_SALES, String.valueOf(System.currentTimeMillis()))
							.toJobParameters();

					execution = jobLauncher.run(rpSalesBatchConfiguration().importRpSalesJob(), rpJobParam);

					jobParams.put(BatchConstants.NAME_RP_SALES, execution.getJobParameters());

				}

				removeProcessedFile(rpSalesFiles, execution.getExitStatus().getExitCode());

				log.info("importRpSalesJob finished with status : " + execution.getStatus());

			}

		} catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
				| JobParametersInvalidException e) {

			handleException(e, "Exception occures while processing " + execution.getJobConfigurationName());
		}
	}

	public void launchStoreBatchJob() {

		log.info("BatchConfiguration.perform() : START");

		try {

			File[] storeFiles = fileUtils().getFiles(localFilePath, KpiFileNames.HRZ_STORE_INFO.name());

			if (!ArrayUtils.isEmpty(storeFiles)) {

				log.info("importStoresJob finished with status : STARTED");

				IS_STORE_JOB_COMPLETED = false;

				boolean isJobRunning = isJobRunning(BatchConstants.NAME_STORE_INFO);

				if (!isJobRunning) {

					JobParameters param = new JobParametersBuilder()
							.addString(BatchConstants.PARAM_STORE_INFO, String.valueOf(System.currentTimeMillis()))
							.toJobParameters();

					execution = jobLauncher.run(storesBatchConfiguration().importStoresJob(), param);

					jobParams.put(BatchConstants.NAME_STORE_INFO, execution.getJobParameters());

				}

				IS_STORE_JOB_COMPLETED = "EXECUTING".equals(execution.getExitStatus().getExitCode()) ? false : true;
				
				String status = (execution!=null) ? execution.getStatus().name():"";
				
				String exitCode = (execution != null)?execution.getExitStatus().getExitCode():"";

				removeProcessedFile(storeFiles, exitCode);
				
				log.info("importStoresJob finished with status :" + status);

			}

		} catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
				| JobParametersInvalidException e) {

			handleException(e, "Exception occures while processing " + execution.getJobConfigurationName());
		}
	}

	public void launchDistrictSales() {

		log.info("importDistrictSalesJob finished with status : STARTED");

		try {

			File[] districtSalesFiles = fileUtils().getFiles(localFilePath,
					KpiFileNames.HRZ_MONTHLY_DISTRICT_IMPROVE.name());

			if (!ArrayUtils.isEmpty(districtSalesFiles)) {

				boolean isJobRunning = isJobRunning(BatchConstants.NAME_DISTRICT_SALES);

				if (!isJobRunning) {

					JobParameters districtSalesJobParam = new JobParametersBuilder()
							.addString(BatchConstants.PARAM_DISTRICT_SALES, String.valueOf(System.currentTimeMillis()))
							.toJobParameters();

					execution = jobLauncher.run(districtSalesConfiguration().importDistrictSalesJob(),
							districtSalesJobParam);

					jobParams.put(BatchConstants.NAME_DISTRICT_SALES, execution.getJobParameters());

				}

				removeProcessedFile(districtSalesFiles, execution.getExitStatus().getExitCode());
			}

		} catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
				| JobParametersInvalidException e) {

			handleException(e, "Exception occures while processing " + execution.getJobConfigurationName());
		}
		
		String status = (execution!=null) ? execution.getStatus().name():"";

		log.info("importDistrictSalesJob finished with status : " +status );

	}

	private boolean isJobRunning(String jobName) {

		JobParameters executedParams = jobParams.get(jobName);
		
		boolean isRunning = false;

		if (executedParams != null) {

			JobExecution lastExecutedJob = jobRepository.getLastJobExecution(jobName, executedParams);
			
			isRunning = (lastExecutedJob != null) ? lastExecutedJob.isRunning() : false;
		}

		return isRunning;
	}

	private void removeProcessedFile(File[] rpSalesFiles, String executionStatus) {

		if (executionStatus.equalsIgnoreCase(BatchStatus.COMPLETED.name())) {

			fileUtils().deleteFiles(rpSalesFiles);
		}
	}

	@Bean
	public TaskExecutor taskExecutor() {

		SimpleAsyncTaskExecutor ex = new SimpleAsyncTaskExecutor();

		ex.setConcurrencyLimit(-1);

		return ex;
	}

	@Bean
	public EntityManagerFactory entityManagerFactory() {

		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();

		vendorAdapter.setGenerateDdl(false);

		LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();

		factory.setJpaVendorAdapter(vendorAdapter);

		factory.setPackagesToScan("com.wba.horizon.persistence");

		factory.setDataSource(dataSource);

		factory.afterPropertiesSet();

		return factory.getObject();
	}

	/**
	 * This method for handling exception for whole horizon batch module
	 * 
	 * @param t
	 * @param message
	 */
	public void handleException(Throwable t, String message) {

		log.error(message, t);

	}
	
public void invalidFileNameLogging(){
		
		@SuppressWarnings("static-access")
		File[] filesList = fileUtils().getFilesList(localFilePath);

		KpiFileNames[] files = KpiFileNames.values();

		for (int i = 0; i < filesList.length; i++) {

			String flag = BatchConstants.FALSE;

			String fileName = filesList[i].getName();

			for (int j = 0; BatchConstants.FALSE.equals(flag) && j < files.length; j++) {

				if ((fileName.endsWith(".txt") || fileName.endsWith(".TXT")) && fileName.startsWith(files[j].name())) {

					flag = BatchConstants.TRUE;

				} else {

					flag = BatchConstants.FALSE;
				}

			}

			if (BatchConstants.FALSE.equals(flag)) {

				if (null != invalidFiles.get(fileName)) {

				} else {
					invalidFiles.put(fileName, fileName);
					log.error("Invalid file name --" + fileName);
					EmailSender.sendMailInvalidFile(fileName, "Invalid file name -"+fileName);
				}

			}
		}
		
	}
}
