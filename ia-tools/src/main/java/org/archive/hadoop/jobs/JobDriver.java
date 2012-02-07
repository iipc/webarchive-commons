package org.archive.hadoop.jobs;

import org.apache.hadoop.util.ProgramDriver;
import org.archive.extract.ResourceExtractor;
import org.archive.hadoop.cdx.CDXClusterRangeDumper;
import org.archive.hadoop.cdx.CDXConverterTool;
import org.archive.hadoop.cdx.HDFSLSR;
import org.archive.hadoop.cdx.HDFSRangeDumper;
import org.archive.hadoop.cdx.ManifestAggregator;
import org.archive.hadoop.cdx.SummaryGenerator;
import org.archive.hadoop.util.HDFSMove;
import org.archive.hadoop.util.HDFSSync;
import org.archive.hadoop.util.HDFSeeko;
import org.archive.io.ZipNumWriterTool;
import org.archive.server.GZRangeClientTool;
import org.archive.server.GZRangeServer;
import org.archive.util.binsearch.FileSearchTool;

public class JobDriver {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int exitCode = -1;
		ProgramDriver pgd = new ProgramDriver();
		try {

			pgd.addClass(CDXConverterTool.TOOL_NAME, 
					CDXConverterTool.class,
					CDXConverterTool.TOOL_DESCRIPTION);

			pgd.addClass(BuildCluster.TOOL_NAME, 
					BuildCluster.class,
					BuildCluster.TOOL_DESCRIPTION);

			pgd.addClass(MergeClusters.TOOL_NAME,
					MergeClusters.class,
					MergeClusters.TOOL_DESCRIPTION);

			pgd.addClass(HTTPImportJob.TOOL_NAME, 
					HTTPImportJob.class,
					HTTPImportJob.TOOL_DESCRIPTION);

//			pgd.addClass("cdx-transform",
//					CDXTransformer.class,
//					"Test tool which trasnforms a wayback CDX into an experimental SURT split format");

			pgd.addClass(HDFSeeko.TOOL_NAME, 
					HDFSeeko.class,
					HDFSeeko.TOOL_DESCRIPTION);

			pgd.addClass(ZipNumWriterTool.TOOL_NAME, 
					ZipNumWriterTool.class,
					ZipNumWriterTool.TOOL_DESCRIPTION);

			pgd.addClass(ManifestAggregator.TOOL_NAME,
					ManifestAggregator.class,
					ManifestAggregator.TOOL_DESCRIPTION);

			pgd.addClass(HDFSMove.TOOL_NAME, HDFSMove.class,
					HDFSMove.TOOL_DESCRIPTION);

			pgd.addClass(HDFSSync.TOOL_NAME, HDFSSync.class,
					HDFSSync.TOOL_DESCRIPTION);

			pgd.addClass(SummaryGenerator.TOOL_NAME, 
					SummaryGenerator.class,
					SummaryGenerator.TOOL_DESCRIPTION);

			pgd.addClass(FileSearchTool.TOOL_NAME,
					FileSearchTool.class,
					FileSearchTool.TOOL_DESCRIPTION);

			pgd.addClass(HDFSRangeDumper.TOOL_NAME,
					HDFSRangeDumper.class,
					HDFSRangeDumper.TOOL_DESCRIPTION);

			pgd.addClass(HDFSLSR.TOOL_NAME, HDFSLSR.class,
					HDFSLSR.TOOL_DESCRIPTION);

			pgd.addClass(ResourceExtractor.TOOL_NAME,
					ResourceExtractor.class,
					ResourceExtractor.TOOL_DESCRIPTION);

			pgd.addClass(CDXClusterRangeDumper.TOOL_NAME,
					CDXClusterRangeDumper.class,
					CDXClusterRangeDumper.TOOL_DESCRIPTION);

			pgd.addClass(MergeClusterRangesJob.TOOL_NAME,
					MergeClusterRangesJob.class,
					MergeClusterRangesJob.TOOL_DESCRIPTION);

			pgd.addClass(GZRangeServer.TOOL_NAME,
					GZRangeServer.class,
					GZRangeServer.TOOL_DESCRIPTION);

			pgd.addClass(GZRangeClientTool.TOOL_NAME,
					GZRangeClientTool.class,
					GZRangeClientTool.TOOL_DESCRIPTION);

			pgd.addClass(GZRangeClientTool.TOOL_NAME,
					GZRangeClientTool.class,
					GZRangeClientTool.TOOL_DESCRIPTION);
			
			pgd.addClass(WATExtractorJob.TOOL_NAME,
					WATExtractorJob.class,
					WATExtractorJob.TOOL_DESCRIPTION);
			
			pgd.driver(args);

			exitCode = 0;
		} catch (Throwable e) {
			e.printStackTrace();
		}

		System.exit(exitCode);
	}

}
