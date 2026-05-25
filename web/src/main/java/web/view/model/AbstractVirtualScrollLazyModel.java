/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;

public abstract class AbstractVirtualScrollLazyModel<T, I extends Serializable> extends LazyDataModel<T> {

    private int first;

    private int pageSize;

    private List<SortMeta> multiSortMeta;

    private Map<String, Object> filters;

    private boolean reset;

    protected abstract Function<T, I> keyFunction();

    protected abstract long count();

    protected abstract List<T> loadData(int first, int pageSize, List<SortMeta> multiSortMeta, Map<String, Object> filters);

    public void reset() {
        reset = true;
        setRowCount((int) count());
    }

    @Override
    public List<T> load(int first, int pageSize, List<SortMeta> multiSortMeta, Map<String, Object> filters) {
        if (reset || first != this.first || pageSize != this.pageSize || !Objects.equals(multiSortMeta, this.multiSortMeta) ||
            !Objects.equals(filters, this.filters)) {
            reset = false;
            this.first = first;
            this.pageSize = pageSize;
            this.multiSortMeta = multiSortMeta;
            this.filters = filters;
            if (getRowCount() == 0) {
                setRowCount((int) count());
            }
            return getRowCount() == 0 ? Collections.emptyList() : loadData(first, pageSize, multiSortMeta, filters);
        } else {
            return (List<T>) getWrappedData();
        }
    }

    @Override
    public T getRowData(String rowKey) {
        return ((List<T>) getWrappedData()).stream().filter(t -> Objects.equals(String.valueOf(getRowKey(t)), rowKey)).findFirst().orElse(null);
    }

    @Override
    public I getRowKey(T object) {
        return keyFunction().apply(object);
    }
}
