# -*- encoding : utf-8 -*-
class NotesController < ApplicationController
  
  def index
    @notes = Note.order('id desc').all
    @note = Note.order('id desc').first
    
    respond_to do |format|
      format.html #index.html.erb
      format.xml { render :xml => @notes }
      format.json {render :json => @note.to_json.to_s.gsub(/\\u([0-9a-z]{4})/) {|s| [$1.to_i(16)].pack("U")} }
    end
  end

  def show
    @note = Note.find(params[:id]) || Note.all.last

   respond_to do |format|
      format.html # index.html.erb
      format.xml  { render :xml => @note }
      format.json { render :json => @note.to_json.to_s.gsub(/\\u([0-9a-z]{4})/) {|s| [$1.to_i(16)].pack("U")} }
    end
  end

  def new
    @note = Note.new
    
    respond_to do |format|
      format.html #new.html.erb
      format.json { render :json => @note.to_json.to_s.gsub(/\\u([0-9a-z]{4})/) {|s| [$1.to_i(16)].pack("U")} }
    end
  end

  def create
    #expire_page "/notes/#{Note.last.id + 1}.json"
    @note = Note.new(params[:note])
    
    respond_to do |format|
      if @note.save
        format.json { render :json => @note.to_json.to_s.gsub(/\\u([0-9a-z]{4})/) {|s| [$1.to_i(16)].pack("U")},
                      :status => :created, :location => @note }
      else
        format.html { render :action => "new" }
        format.json { render :json => @note.errors.to_json.to_s.gsub(/\\u([0-9a-z]{4})/) {|s| [$1.to_i(16)].pack("U")},
                      :status => :unprocessable_entity }
      end
    end
  end

  def edit
    @note = Note.find(params[:id])
  end

  def update
    @note = Note.find(params[:id])
    
    respond_to do |format|
      if @note.update_attributes(params[:note])
        format.html { redirect_to(@note,
                      :notice => 'Note updated successfully.')}
        format.json { head :no_content }
      else
        format.html { render :action => "edit" }
        format.json { render :json => @note.errors.to_json.to_s.gsub(/\\u([0-9a-z]{4})/) {|s| [$1.to_i(16)].pack("U")},
                      :status => :unprocessable_entity }
      end
    end
  end

  def destroy
    @note = Note.find(params[:id])
    @note.destroy
    
    respond_to do |format|
      format.html { redirect_to notes_url }
      format.json { head :no_content }
    end
  end

end
