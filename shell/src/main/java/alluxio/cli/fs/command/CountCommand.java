/*
 * The Alluxio Open Foundation licenses this work under the Apache License, version 2.0
 * (the "License"). You may not use this work except in compliance with the License, which is
 * available at www.apache.org/licenses/LICENSE-2.0
 *
 * This software is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied, as more fully set forth in the License.
 *
 * See the NOTICE file distributed with this work for information regarding copyright ownership.
 */

package alluxio.cli.fs.command;

import alluxio.AlluxioURI;
import alluxio.annotation.PublicApi;
import alluxio.cli.CommandUtils;
import alluxio.client.file.FileSystemContext;
import alluxio.client.file.URIStatus;
import alluxio.exception.AlluxioException;
import alluxio.exception.status.InvalidArgumentException;

import org.apache.commons.cli.CommandLine;

import java.io.IOException;
import java.util.List;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Displays the number of folders and files matching the specified prefix in args.
 */
@ThreadSafe
@PublicApi
public final class CountCommand extends AbstractFileSystemCommand {

  /**
   * @param fsContext the filesystem of Alluxio
   */
  public CountCommand(FileSystemContext fsContext) {
    super(fsContext);
  }

  @Override
  public String getCommandName() {
    return "count";
  }

  @Override
  public void validateArgs(CommandLine cl) throws InvalidArgumentException {
    CommandUtils.checkNumOfArgsEquals(this, cl, 1);
  }

  @Override
  public int run(CommandLine cl) throws AlluxioException, IOException {
    String[] args = cl.getArgs();
    AlluxioURI inputPath = new AlluxioURI(args[0]);

    long[] values = countHelper(inputPath);
    String format = "%-25s%-25s%-15s%n";
    System.out.format(format, "File Count", "Folder Count", "Total Bytes");
    System.out.format(format, values[0], values[1], values[2]);
    return 0;
  }

  private long[] countHelper(AlluxioURI path) throws AlluxioException, IOException {
    URIStatus status = mFileSystem.getStatus(path);

    if (!status.isFolder()) {
      return new long[]{ 1L, 0L, status.getLength() };
    }

    long[] rtn = new long[]{ 0L, 1L, 0L };

    List<URIStatus> statuses;
    try {
      statuses = mFileSystem.listStatus(path);
    } catch (AlluxioException e) {
      throw new IOException(e.getMessage());
    }
    for (URIStatus uriStatus : statuses) {
      long[] toAdd = countHelper(new AlluxioURI(uriStatus.getPath()));
      rtn[0] += toAdd[0];
      rtn[1] += toAdd[1];
      rtn[2] += toAdd[2];
    }
    return rtn;
  }

  @Override
  public String getUsage() {
    return "count <path>";
  }

  @Override
  public String getDescription() {
    return "Displays the number of files and directories matching the specified prefix.";
  }
}
