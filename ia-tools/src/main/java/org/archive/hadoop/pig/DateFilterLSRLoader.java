package org.archive.hadoop.pig;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.mapreduce.JobContext;

public class DateFilterLSRLoader extends LSRPigLoader {
	
	protected DateFilter filter;
	protected String dateFilterParam;

	public DateFilterLSRLoader(String filterParam, String param) {
		super(param);
		dateFilterParam = filterParam;
	}

	@Override
	public List<FileStatus> lsrFiltered(String location, JobContext job) throws IOException
	{
		filter = new DateFilter();
		
		try {
			filter.init(dateFilterParam, job.getConfiguration());
			return lsrFiltered(location, job, filter);
		} finally {
			filter.close();
		}
	}
}
