/*
 *	The MIT License (MIT)
 *
 *	Copyright (c) 2014 Mihail K
 *
 *	Permission is hereby granted, free of charge, to any person obtaining a copy
 *	of this software and associated documentation files (the "Software"), to deal
 *	in the Software without restriction, including without limitation the rights
 *	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *	copies of the Software, and to permit persons to whom the Software is
 *	furnished to do so, subject to the following conditions:
 *
 *	The above copyright notice and this permission notice shall be included in all
 *	copies or substantial portions of the Software.
 *
 *	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *	SOFTWARE.
 */

package io.cloudchaser.murmur.types;

import static io.cloudchaser.murmur.types.MurmurType.ARRAY;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Mihail K
 * @since 0.1
 */
public class MurmurArray extends MurmurObject
		implements ReferenceType {
	
	private final List<MurmurObject> elements;

	public MurmurArray() {
		super(ARRAY);
		elements = new ArrayList<>();
	}

	public MurmurArray(List<MurmurObject> elements) {
		super(ARRAY);
		this.elements = elements;
	}

	@Override
	public MurmurObject getMember(String name) {
		switch(name) {
			case "length":
				// Array length property.
				return asInteger();
			case "reverse":
				// Reverse the array, in-place.
				Collections.reverse(elements);
				return this;
			case "clone":
				// Create a shadow copy.
				return new MurmurArray(new ArrayList<>(elements));
			case "string":
				// Return a string form.
				return asString();
			default:
				// Delegate to parent.
				return super.getMember(name);
		}
	}
	
	@Override
	public boolean isCompatible(Class<?> type) {
		return type.isAssignableFrom(Object[].class) ||
				type.isAssignableFrom(MurmurObject[].class) ||
				type.isAssignableFrom(HashSet.class) ||
				type.isAssignableFrom(ArrayList.class) ||
				type.isAssignableFrom(LinkedList.class);
	}
	
	@Override
	public Object getAsJavaType(Class<?> type) {
		// Java array type.
		if(type.isAssignableFrom(Object[].class) ||
				type.isAssignableFrom(MurmurObject[].class)) {
			Object array = Array.newInstance(type, elements.size());
			for(int idx = 0; idx < elements.size(); idx++) {
				// Shallow case for list containing itself.
				MurmurObject element = elements.get(idx);
				Array.set(array, idx, element == this ? array :
						element.getAsJavaType(type.isArray() ? 
								type.getComponentType() : type));
			}
			
			// Return the completed array.
			return array;
		}

		// Java list type.
		Collection col;
		if(type.isAssignableFrom(HashSet.class)) {
			col = new HashSet<>();
		} else if(type.isAssignableFrom(ArrayList.class)) {
			col = new ArrayList<>();
		} else if(type.isAssignableFrom(LinkedList.class)) {
			col = new LinkedList<>();
		} else {
			// TODO
			throw new UnsupportedOperationException();
		}
		
		// Populate the collection.
		elements.stream().forEach((element) -> {
			// Shallow case for list containing itself.
			col.add(element == this ? col :
					element.getAsJavaType(Object.class));
		});
		return col;
	}

	@Override
	public MurmurInteger asInteger() {
		return MurmurInteger.create(elements.size());
	}

	@Override
	public MurmurDecimal asDecimal() {
		return MurmurDecimal.create(elements.size());
	}

	@Override
	public MurmurString asString() {
		// Create a string builder for the conversion.
		Iterator<MurmurObject> itr;
		StringBuilder builder = new StringBuilder("[");
		for(itr = elements.iterator(); itr.hasNext();) {
			// TODO : Better way of getting the string value.
			builder.append(itr.next().asString().getValue());
			
			// Separator.
			if(itr.hasNext()) {
				builder.append(", ");
			}
		}
		builder.append("]");
		return MurmurString.create(builder.toString());
	}

	@Override
	public MurmurObject opPlus(MurmurObject other) {
		// Create a copy and append the element.
		List<MurmurObject> copy = new ArrayList<>(elements);
		copy.add(other);
		
		// Return the new array.
		return new MurmurArray(copy);
	}

	@Override
	public MurmurObject opMinus(MurmurObject other) {
		// Create a copy, and remove the element.
		List<MurmurObject> copy = new ArrayList<>(elements);
		copy.remove(other);
		
		// Return the new array.
		return new MurmurArray(copy);
	}

	@Override
	public MurmurObject opEquals(MurmurObject other) {
		// Check for supported type.
		if(other.getType() == ARRAY) {
			// Compare array contents for equality.
			return MurmurBoolean.create(
					elements.equals(((MurmurArray)other).elements));
		}
		
		// Not equal.
		return MurmurBoolean.FALSE;
	}

	@Override
	public MurmurObject opNotEquals(MurmurObject other) {
		// Check for supported type.
		if(other.getType() == ARRAY) {
			// Compare array contents for equality.
			return MurmurBoolean.create(!
					elements.equals(((MurmurArray)other).elements));
		}
		
		// Not equal.
		return MurmurBoolean.TRUE;
	}

	@Override
	public MurmurObject opIndex(MurmurObject other) {
		// Check for supported type.
		if(other.getType().numeric) {
			// TODO : Return an element reference.
			long index = other.asInteger().getValue();
			
			// Check for valid index.
			if(index >= elements.size() ||
					index < -elements.size()) {
				throw new IndexOutOfBoundsException();
			}
			
			// Handle negative indexing.
			if(index > 0) {
				return elements.get((int)index);
			} else {
				index = elements.size() + index;
				return elements.get((int)index);
			}
		}
		
		// Unsupported.
		throw new UnsupportedOperationException();
	}

	@Override
	public MurmurObject opConcat(MurmurObject other) {
		// Check for array type.
		if(other.getType() == ARRAY) {
			// Create a new array, and append the other.
			List<MurmurObject> copy = new ArrayList<>(elements);
			copy.addAll(((MurmurArray)other).elements);
			
			// Return the new array.
			return new MurmurArray(copy);
		}
		
		// Concat single element.
		return opPlus(other);
	}

	@Override
	public MurmurObject opPlusAssign(MurmurObject other) {
		// Append the element.
		elements.add(other);
		return this;
	}

	@Override
	public MurmurObject opMinusAssign(MurmurObject other) {
		// Remove the element.
		elements.remove(other);
		return this;
	}

	@Override
	public MurmurObject opMultiplyAssign(MurmurObject other) {
		// Unused. Arrays don't support integer arithmetic.
		throw new UnsupportedOperationException();
	}

	@Override
	public MurmurObject opDivideAssign(MurmurObject other) {
		// Unused. Arrays don't support integer arithmetic.
		throw new UnsupportedOperationException();
	}

	@Override
	public MurmurObject opModuloAssign(MurmurObject other) {
		// Unused. Arrays don't support integer arithmetic.
		throw new UnsupportedOperationException();
	}

	@Override
	public MurmurObject opBitAndAssign(MurmurObject other) {
		// Unused. Arrays don't support bit arithmetic.
		throw new UnsupportedOperationException();
	}

	@Override
	public MurmurObject opBitXorAssign(MurmurObject other) {
		// Unused. Arrays don't support bit arithmetic.
		throw new UnsupportedOperationException();
	}

	@Override
	public MurmurObject opBitOrAssign(MurmurObject other) {
		// Unused. Arrays don't support bit arithmetic.
		throw new UnsupportedOperationException();
	}

	@Override
	public MurmurObject opShiftLeftAssign(MurmurObject other) {
		// Unused. Arrays don't support bitshift.
		throw new UnsupportedOperationException();
	}

	@Override
	public MurmurObject opShiftRightAssign(MurmurObject other) {
		// Unused. Arrays don't support bitshift.
		throw new UnsupportedOperationException();
	}

	@Override
	public MurmurObject opConcatAssign(MurmurObject other) {
		if(other.getType() == ARRAY) {
			// Append all elements.
			elements.addAll(((MurmurArray)other).elements);
		} else {
			// Append the element.
			elements.add(other);
		}
		return this;
	}

	@Override
	public int hashCode() {
		return elements.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if(!(o instanceof MurmurArray)) return false;
		return ((MurmurArray)o).elements.equals(elements);
	}

	@Override
	public String toString() {
		return "MurmurArray{elements=" + elements + '}';
	}
	
}
