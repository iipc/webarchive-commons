package org.archive.resource.arc.record;

//import java.util.logging.Logger;

import org.archive.format.arc.FiledescRecord;
import org.archive.resource.AbstractEmptyResource;
import org.archive.resource.MetaData;
import org.archive.resource.ResourceConstants;
import org.archive.resource.ResourceContainer;

public class FiledescResource extends AbstractEmptyResource implements ResourceConstants {
//	private static final Logger LOG = 
//		Logger.getLogger(FiledescResource.class.getName()); 

	public FiledescResource(MetaData metaData, ResourceContainer container,
			FiledescRecord record) {
		super(metaData, container);
		metaData.putLong(FILEDESC_MAJOR, record.getMajorVersion());
		metaData.putLong(FILEDESC_MINOR, record.getMinorVersion());
		metaData.putString(FILEDESC_ORGANIZATION, record.getOrganization());
		metaData.putString(FILEDESC_FORMAT, record.getFormat());
		if(record.hasMetaData()) {
			int count = record.getMetaDataCount();
			for(int i = 0; i < count; i++) {
				String name = record.getMetaDataName(i);
				String value = record.getMetaDataValue(i);
				if((name != null) && (value != null)) {
					metaData.appendObj(FILEDESC_DATA,
							METADATA_KV_NAME,name,METADATA_KV_VALUE,value);
				}
			}
		}
	}

}
