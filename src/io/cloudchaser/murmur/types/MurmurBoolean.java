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

import static io.cloudchaser.murmur.types.MurmurType.BOOLEAN;

/**
 *
 * @author Mihail K
 * @since 0.1
 */
public class MurmurBoolean extends MurmurObject {
	
	public static final MurmurBoolean TRUE = new MurmurBoolean(true);
	public static final MurmurBoolean FALSE = new MurmurBoolean(false);
	
	private final boolean value;

	private MurmurBoolean(boolean value) {
		super(BOOLEAN);
		this.value = value;
	}
	
	public boolean getValue() {
		return value;
	}

	@Override
	public MurmurInteger asInteger() {
		return value ? new MurmurInteger(1) : MurmurInteger.ZERO;
	}

	@Override
	public MurmurObject opPlus(MurmurObject other) {
		// Booleans don't support integer arithmetic.
		throw new UnsupportedOperationException();
	}

	@Override
	public MurmurObject opMinus(MurmurObject other) {
		// Booleans don't support integer arithmetic.
		throw new UnsupportedOperationException();
	}

	@Override
	public MurmurObject opMultiply(MurmurObject other) {
		// Booleans don't support integer arithmetic.
		throw new UnsupportedOperationException();
	}

	@Override
	public MurmurObject opDivide(MurmurObject other) {
		// Booleans don't support integer arithmetic.
		throw new UnsupportedOperationException();
	}

	@Override
	public MurmurObject opModulo(MurmurObject other) {
		// Booleans don't support integer arithmetic.
		throw new UnsupportedOperationException();
	}

	@Override
	public MurmurObject opEquals(MurmurObject other) {
		// Check for supported operation.
		if(other.getType() == BOOLEAN) {
			boolean result = value == ((MurmurBoolean)other).value;
			return result ? TRUE : FALSE;
		}
		
		// Not equal.
		return FALSE;
	}

	@Override
	public MurmurObject opNotEquals(MurmurObject other) {
		// Check for supported operation.
		if(other.getType() == BOOLEAN) {
			boolean result = value != ((MurmurBoolean)other).value;
			return result ? TRUE : FALSE;
		}
		
		// Not equal.
		return TRUE;
	}

	@Override
	public String toString() {
		return "MurmurBoolean{" + "value=" + value + '}';
	}
	
}