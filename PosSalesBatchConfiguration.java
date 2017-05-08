package com.wba.horizon.batch.modules.kpi.config;

import java.util.List;

import javax.sql.DataSource;

import org.mockito.InjectMocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.wba.horizon.batch.modules.kpi.dao.POSBatchRepository;
import com.wba.horizon.batch.modules.kpi.listener.PosBatchExecutionListener;
import com.wba.horizon.batch.modules.kpi.listener.RpSalesJobExecutionListener;
import com.wba.horizon.batch.modules.kpi.model.DailySalesDTO;
import com.wba.horizon.batch.modules.kpi.processor.PosBatchProcessor;
import com.wba.horizon.batch.modules.kpi.processor.RpSalesItemProcessor;
import com.wba.horizon.batch.modules.kpi.util.BatchConstants;
import com.wba.horizon.persistence.DailyPOSDeptSales;
import com.wba.horizon.persistence.POSDeptSales;
import com.wba.horizon.persistence.PosDailySales;
import com.wba.horizon.persistence.RpSales;
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties
@EnableBatchProcessing
public class PosSalesBatchConfiguration {
	private static final Logger log = LoggerFactory.getLogger(AreaBatchConfiguration.class);
	
	@Autowired
	POSBatchRepository pOSBatchRepository;
	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	private BatchConfiguration batchConfiguration;
	

	@Autowired
	public StepBuilderFactory stepBuilderFactory;
	
	 @InjectMocks
	 private JobBuilderFactory jobs;
	  
	
	 ItemStreamReader<POSDeptSales> POsSalesReader() {
		
		
		 String queryString="from POSDeptSales";
		 JpaPagingItemReader<POSDeptSales> reader = new JpaPagingItemReader<POSDeptSales>();
		    reader.setEntityManagerFactory(batchConfiguration.entityManagerFactory());
		    reader.setQueryString(queryString);
		   
		    return reader;
	 
	      
	    }
	 
	  
	 public POSDeptSales getAllData() throws UnexpectedInputException, ParseException, NonTransientResourceException, Exception{
		 PosSalesBatchConfiguration pos=new PosSalesBatchConfiguration();
		 ItemStreamReader<POSDeptSales> itemPos=pos.POsSalesReader();
		 POSDeptSales pOSDeptSales= itemPos.read();
		 return pOSDeptSales;
	 }
	 
	@Bean
	public Job importPosSalesJob() {
		
		Job importPosSalesJob = null;
		
		try{

			importPosSalesJob = jobBuilderFactory.get(BatchConstants.NAME_POS_BATCH)
				.listener(PosSalesListener()).flow(step1()).end().build();
		
		}catch(Throwable t){
			
			batchConfiguration.handleException(t, "Error Occures while contructing importRpSalesJob");
		}

		  return importPosSalesJob;
	}
	
	
	@SuppressWarnings("unchecked")
	 @Bean  
	 
	 public Step step1() {  
		
	      return stepBuilderFactory.get("step1").<POSDeptSales,PosDailySales>chunk(5)
	                .reader(POsSalesReader())
	                .processor(posSalesProcessor()).build(); 
	  }
	@Bean
	public PosBatchProcessor posSalesProcessor() {
		return new PosBatchProcessor();
	}

	
	public JobExecutionListener PosSalesListener() {

		return new PosBatchExecutionListener(this.batchConfiguration);
	}
}
