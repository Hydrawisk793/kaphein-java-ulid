package kaphein.ulid;

interface InterruptableCallable<R>
{
  R call()
    throws InterruptedException;
}
