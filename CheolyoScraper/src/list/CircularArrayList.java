package list;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An array-based list is a contiguous-memory representation of the List
 * abstract data type. This array-based list dynamically resizes to ensure O(1)
 * amortized cost for adding to the end of the list. Size is maintained as a
 * global field to allow for O(1) size() and isEmpty() behaviors.
 * 
 * @author Dr. King
 *
 * @param <E> the type of elements stored in the list
 */
public class CircularArrayList<E> implements List<E> {

	/**
	 * The initial capacity of the list if the client does not provide a capacity
	 * when constructing an instance of the array-based list
	 **/
	private final static int DEFAULT_CAPACITY = 0;

	/** The array in which elements will be stored **/
	private E[] data;

	/** The number of elements stored in the array-based list data structure **/
	private int size;

	/**
	 * A reference to the index of the first element in the queue
	 */
	private int front;

	/**
	 * A reference to the index immediately after the last element in the queue
	 */
	private int rear;

	/**
	 * Constructs a new instance of an array-based list data structure with the
	 * default initial capacity of the internal array
	 */
	public CircularArrayList() {
		this(DEFAULT_CAPACITY);
	}

	/**
	 * Constructs a new instance of an array-based list data structure with the
	 * provided initial capacity
	 * 
	 * @param capacity the initial capacity of the internal array used to store the
	 *                 list elements
	 */
	@SuppressWarnings("unchecked")
	public CircularArrayList(int capacity) {
		data = (E[]) (new Object[capacity]);
		size = 0;
		front = 0;
		rear = 0;
	}

	@Override
	public void add(int index, E element) {
		checkIndexForAdd(index);
		ensureCapacity(++size + 1);
		int intCircularIndex = (index + front) % data.length;
		int i = rear;
		while (intCircularIndex != i) {
			data[(i < 0) ? i + data.length : i] = data[(i - 1 < 0) ? i - 1 + data.length : i - 1];
			i--;
		}
		rear = (rear + 1) % data.length;
		data[intCircularIndex] = element;
	}

	@Override
	public E get(int index) {
		checkIndex(index);
		return data[index + front];
	}

	@Override
	public E remove(int index) {
		checkIndex(index);
		size--;
		int intCircularIndex = (index + front) % data.length;
		E eReturn = data[intCircularIndex];
		while (intCircularIndex != rear) {
			data[intCircularIndex % data.length] = data[(intCircularIndex + 1) % data.length];
			intCircularIndex++;
		}
		rear = (rear - 1 < 0) ? rear - 1 + data.length : rear - 1;
		return eReturn;
	}

	@Override
	public E set(int index, E element) {
		checkIndex(index);
		E eReturn = data[index + front];
		data[index + front] = element;
		return eReturn;
	}

	@Override
	public int size() {
		return size;
	}

	/**
	 * To ensure amortized O(1) cost for adding to the array-based queue, use the
	 * doubling strategy on each resize. Here, we add +1 after doubling to handle
	 * the special case where the initial capacity is 0 (otherwise, 0*2 would still
	 * produce a capacity of 0).
	 * 
	 * @param minCapacity the minimum capacity that must be supported by the
	 *                    internal array
	 */
	private void ensureCapacity(int minCapacity) {
		int oldCapacity = data.length;
		if (minCapacity > oldCapacity) {
			int newCapacity = (oldCapacity * 2) + 1;
			if (newCapacity < minCapacity) {
				newCapacity = minCapacity;
			}
			@SuppressWarnings("unchecked")
			E[] newData = (E[]) (new Object[newCapacity]);
			// Remember that we cannot copy an array the same way we do
			// when resizing an array-based list since we do not want to
			// "break" the elements in the queue since there may be wrap-around
			// at the end of the array
			for (int i = 0; i < size - 1; i++) {
				newData[i] = data[(i + front) % size];
			}
			data = newData;
			rear -= front;
			front = 0;
		}
	}

	@Override
	public Iterator<E> iterator() {
		return new ElementIterator();
	}

	@Override
	public void addFirst(E element) {
		ensureCapacity(++size + 1);
		if (--front < 0) {
			front += data.length;
		}
		data[front] = element;
	}

	@Override
	public void addLast(E element) {
		ensureCapacity(++size + 1);
		data[rear++] = element;
		rear %= data.length;
	}

	@Override
	public E first() {
		return data[front];
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	public E last() {
		return data[rear - 1];
	}

	@Override
	public E removeFirst() {
		checkIndex(front);
		E returnE = data[front];
		front = (front + 1) % data.length;
		size--;
		return returnE;
	}

	@Override
	public E removeLast() {
		checkIndex(rear - 1);
		if (--rear < 0) {
			front += data.length;
		}
		E returnE = data[rear];
		size--;
		return returnE;
	}

	/**
	 * An Iterator that iterates through the elements of this type of List.
	 * 
	 * @author Alexander Chen
	 */
	private class ElementIterator implements Iterator<E> {
		/** Position within the list this iterator is at. */
		private int position;
		/** Stores whether or not it is appropriate to remove the element. */
		private boolean removeOK;

		/**
		 * Construct a new element iterator where the cursor is initialized to the
		 * beginning of the list.
		 */
		public ElementIterator() {
			position = front - 1;
		}

		@Override
		public boolean hasNext() {
			return position != rear - 1;
		}

		@Override
		public E next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			removeOK = true;
			position = (position + 1) % data.length;
			E eReturn = data[position];
			return eReturn;
		}

		@Override
		public void remove() {
			if (!removeOK) {
				throw new IllegalStateException();
			}
			for (int i = position; i < size - 1; i++) {
				data[i] = data[i + 1];
			}
			position--;
			rear--;
			if (rear < 0) {
				rear += data.length;
			}
			size--;
			removeOK = false;
		}
	}

	/**
	 * Checks whether the provided index is a legal index based on the current state
	 * of the list. This check should be performed when accessing any specific
	 * indexes within the list.
	 * 
	 * @param index the index for which to check whether it is valid/legal in the
	 *              current list or not
	 */
	private void checkIndex(int index) {
		if (index < 0 || index >= size()) {
			throw new IndexOutOfBoundsException("Index is invalid: " + index + " (size=" + size() + ")");
		}
	}

	/**
	 * Checks whether the provided index is a legal index based on the current state
	 * of the list. This check should be performed when adding elements at specific
	 * indexes within the list.
	 * 
	 * @param index the index for which to check whether it is valid/legal in the
	 *              current list or not
	 */
	private void checkIndexForAdd(int index) {
		if (index < 0 || index > size()) {
			throw new IndexOutOfBoundsException("Index is invalid: " + index + " (size=" + size() + ")");
		}
	}

}
