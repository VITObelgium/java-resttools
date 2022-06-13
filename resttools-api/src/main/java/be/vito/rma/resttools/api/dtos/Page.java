package be.vito.rma.resttools.api.dtos;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * @author (c) 2020 Stijn.VanLooy@vito.be
 *
 */
public class Page<T> {

	// total amount of elements (in all pages)
	@Getter @Setter private long totalCount;

	// page index
	@Getter @Setter private int offset;

	// maximum number of elements per page
	// only the last page might have less elements than the value for limit,
	// all other pages will have this amount of elements
	@Getter @Setter private int limit;

	@Getter @Setter private List<T> data;

}
