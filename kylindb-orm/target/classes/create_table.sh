#!/bin/sh
#how to run:  env COMPRESSION=NONE HBASE_HOME=path/to/hbase-0.94.X ./create_table.sh

test -n "$HBASE_HOME" || {
  echo >&2 'The environment variable HBASE_HOME must be set'
  exit 1
}
test -d "$HBASE_HOME" || {
  echo >&2 "No such directory: HBASE_HOME=$HBASE_HOME"
  exit 1
}

METRICS_TABLE=${METRICS_TABLE-'metrics'}
NOTES_TABLE=${NOTES_TABLE-'notes'}
OFFSETS_TABLE=${OFFSETS_TABLE-'offsets'}
BLOOMFILTER=${BLOOMFILTER-'ROW'}
# LZO requires lzo2 64bit to be installed + the hadoop-gpl-compression jar.
COMPRESSION=${COMPRESSION-'SNAPPY'}
# All compression codec names are upper case (NONE, LZO, SNAPPY, etc).
COMPRESSION=`echo "$COMPRESSION" | tr a-z A-Z`

case $COMPRESSION in
  (NONE|LZO|GZIP|SNAPPY)  :;;  # Known good.
  (*)
    echo >&2 "warning: compression codec '$COMPRESSION' might not be supported."
    ;;
esac

# HBase scripts also use a variable named `HBASE_HOME', and having this
# variable in the environment with a value somewhat different from what
# they expect can confuse them in some cases.  So rename the variable.
hbh=$HBASE_HOME
unset HBASE_HOME
exec "$hbh/bin/hbase" shell <<EOF
create '$METRICS_TABLE',
  {NAME => 'm', VERSIONS => 1, COMPRESSION => '$COMPRESSION', BLOOMFILTER => '$BLOOMFILTER'}

create '$NOTES_TABLE',
  {NAME => 'n', VERSIONS => 1, COMPRESSION => '$COMPRESSION', BLOOMFILTER => '$BLOOMFILTER'}

create '$OFFSETS_TABLE',
  {NAME => 'o', VERSIONS => 1, COMPRESSION => '$COMPRESSION', BLOOMFILTER => '$BLOOMFILTER'}
EOF
